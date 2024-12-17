package me.snoty.backend.wiring.flow.execution

import com.mongodb.MongoWriteException
import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.mongoField
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.wiring.flow.FlowFeatureFlags
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.MongoWorkflow
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.FLOW_EXECUTION_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.bson.codecs.pojo.annotations.BsonId
import org.koin.core.annotation.Single
import java.util.UUID
import java.util.concurrent.TimeUnit

@Single
class MongoFlowExecutionService(mongoDB: MongoDatabase, featureFlags: FlowFeatureFlags) : FlowExecutionService {
	private val logger = KotlinLogging.logger {}
	private val collection = mongoDB.getCollection<FlowLogs>(FLOW_EXECUTION_COLLECTION_NAME)

	init {
		@OptIn(DelicateCoroutinesApi::class) // the indexes are non-critical and should just *eventually* be created
		GlobalScope.launch(Dispatchers.IO) {
			val expirationIndex = FlowLogs::creationDate.name

			collection.createIndexes(
				listOf(
					IndexModel(Indexes.descending(FlowLogs::flowId.name)),
					IndexModel(
						Indexes.descending(FlowLogs::creationDate.name),
						IndexOptions()
							.name(expirationIndex)
							.expireAfter(featureFlags.expirationSeconds, TimeUnit.SECONDS)
					),
					IndexModel(Indexes.descending(FlowLogs::flowId.name, FlowLogs::creationDate.name)),
				),
			).retryWhen { cause, attempt ->
				if (attempt > 3) {
					logger.error(cause) { "Failed to create indexes on ${collection.namespace.fullName}" }
					return@retryWhen false
				}

				logger.debug { "Dropping index $expirationIndex on ${collection.namespace.fullName} because the expiration likely changed" }
				collection.dropIndex(expirationIndex)

				true
			}.collect()
		}
	}

	override suspend fun create(jobId: String, flowId: NodeId, triggeredBy: FlowTriggerReason) {
		runCatching {
			collection.insertOne(
				FlowLogs(
					_id = jobId,
					flowId = flowId,
					triggeredBy = triggeredBy,
					creationDate = Clock.System.now(),
					status = FlowExecutionStatus.RUNNING,
					logs = listOf()
				)
			)
		}.onFailure {
			// E11000 duplicate key error
			if (it !is MongoWriteException || it.code != 11000) throw it

			// was inserted already (by a previous attempt), let's just update that doc
			collection.upsertOne(
				Filters.eq(FlowLogs::_id.name, jobId),
				Updates.combine(
					Updates.set(FlowLogs::creationDate.name, Clock.System.now()),
					Updates.set(FlowLogs::status.name, FlowExecutionStatus.RUNNING),
				)
			)
		}
	}

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) {
		collection.updateOne(
			Filters.eq(FlowLogs::_id.name, jobId),
			Updates.set(FlowLogs::status.name, status),
		)
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry) {
		collection.updateOne(
			Filters.eq(FlowLogs::_id.name, jobId),
			Updates.push(FlowLogs::logs.name, entry),
		)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry> =
		collection.find(Filters.eq(FlowLogs::flowId.name, flowId))
			.toList()
			.flatMap { it.logs }

	override fun query(userId: UUID): Flow<EnumeratedFlowExecution> {
		val flow = "flow"

		data class MongoFlowExecution(
			val jobId: String,
			@BsonId
			val flowId: NodeId,
			val status: FlowExecutionStatus,
			val triggeredBy: FlowTriggerReason?,
			val startDate: Instant,
		) {
			fun toFlowExecution() = EnumeratedFlowExecution(
				jobId = jobId,
				flowId = flowId,
				triggeredBy = triggeredBy ?: FlowTriggerReason.Unknown,
				startDate = startDate,
				status = status,
			)
		}

		return collection.aggregate<MongoFlowExecution>(
			Aggregates.sort(Sorts.descending(FlowLogs::flowId.name, FlowLogs::creationDate.name)),
			Aggregates.group(
				FlowLogs::flowId.mongoField,
				Accumulators.first(MongoFlowExecution::jobId.name, FlowLogs::_id.mongoField),
				Accumulators.first(MongoFlowExecution::flowId.name, FlowLogs::flowId.mongoField),
				Accumulators.first(MongoFlowExecution::status.name, FlowLogs::status.mongoField),
				Accumulators.first(MongoFlowExecution::startDate.name, FlowLogs::creationDate.mongoField),
			),
			Aggregates.lookup(
				/* from = */ FLOW_COLLECTION_NAME,
				// _id as the group makes the flowId be the _id
				/* localField = */ FlowLogs::_id.name,
				/* foreignField = */ MongoWorkflow ::_id.name,
			/* as = */ flow,
			),
			Aggregates.match(Filters.elemMatch(flow, Filters.eq(MongoWorkflow::userId.name, userId))),
			Aggregations.project(
				Projections.exclude(flow),
			),
		).map { it.toFlowExecution() }
	}

	override fun query(flowId: NodeId, startFrom: String?, limit: Int): Flow<FlowExecution> =
		collection.find(
			Filters.and(
				Filters.eq(FlowLogs::flowId.name, flowId),
				if (startFrom != null) Filters.lt(FlowLogs::_id.name, startFrom) else Filters.empty(),
			)
		)
			.sort(Sorts.descending(FlowLogs::creationDate.name))
			.limit(limit)
			.map {
				it.run {
					FlowExecution(
						jobId = _id,
						flowId = flowId,
						triggeredBy = triggeredBy ?: FlowTriggerReason.Unknown,
						startDate = creationDate,
						status = status,
						logs = logs,
					)
				}
			}

	override suspend fun deleteAll(flowId: NodeId) {
		collection.deleteMany(Filters.eq(FlowLogs::flowId.name, flowId))
	}
}
