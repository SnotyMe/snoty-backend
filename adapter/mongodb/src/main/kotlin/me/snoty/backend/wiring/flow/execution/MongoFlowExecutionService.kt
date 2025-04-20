package me.snoty.backend.wiring.flow.execution

import com.mongodb.MongoWriteException
import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.mongoField
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.FlowFeatureFlags
import me.snoty.backend.wiring.flow.MongoWorkflow
import me.snoty.integration.common.wiring.flow.*
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit

data class MongoFlowLogs(
	val _id: String,
	val flowId: ObjectId,
	val triggeredBy: FlowTriggerReason?,
	val creationDate: Instant,
	val status: FlowExecutionStatus? = null,
	val logs: List<NodeLogEntry>,
)

@Single
class MongoFlowExecutionService(mongoDB: MongoDatabase, featureFlags: FlowFeatureFlags) : FlowExecutionService {
	private val logger = KotlinLogging.logger {}
	private val collection = mongoDB.getCollection<MongoFlowLogs>(FLOW_EXECUTION_COLLECTION_NAME)

	init {
		@OptIn(DelicateCoroutinesApi::class) // the indexes are non-critical and should just *eventually* be created
		GlobalScope.launch(Dispatchers.IO) {
			val expirationIndex = MongoFlowLogs::creationDate.name

			collection.createIndexes(
				listOf(
					IndexModel(Indexes.descending(MongoFlowLogs::flowId.name)),
					IndexModel(
						Indexes.descending(MongoFlowLogs::creationDate.name),
						IndexOptions()
							.name(expirationIndex)
							.expireAfter(featureFlags.expirationSeconds, TimeUnit.SECONDS)
					),
					IndexModel(Indexes.descending(MongoFlowLogs::flowId.name, MongoFlowLogs::creationDate.name)),
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
				MongoFlowLogs(
					_id = jobId,
					flowId = ObjectId(flowId),
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
				Filters.eq(MongoFlowLogs::_id.name, jobId),
				Updates.combine(
					Updates.set(MongoFlowLogs::creationDate.name, Clock.System.now()),
					Updates.set(MongoFlowLogs::status.name, FlowExecutionStatus.RUNNING),
				)
			)
		}
	}

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) {
		collection.updateOne(
			Filters.eq(MongoFlowLogs::_id.name, jobId),
			Updates.set(MongoFlowLogs::status.name, status),
		)
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry) {
		collection.updateOne(
			Filters.eq(MongoFlowLogs::_id.name, jobId),
			Updates.push(MongoFlowLogs::logs.name, entry),
		)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry> =
		collection.find(Filters.eq(MongoFlowLogs::flowId.name, ObjectId(flowId)))
			.toList()
			.flatMap { it.logs }

	override fun query(userId: String): Flow<EnumeratedFlowExecution> {
		val flow = "flow"

		data class MongoFlowExecution(
			val jobId: String,
			@BsonId
			val flowId: ObjectId,
			val status: FlowExecutionStatus,
			val triggeredBy: FlowTriggerReason?,
			val startDate: Instant,
		) {
			fun toFlowExecution() = EnumeratedFlowExecution(
				jobId = jobId,
				flowId = flowId.toHexString(),
				triggeredBy = triggeredBy ?: FlowTriggerReason.Unknown,
				startDate = startDate,
				status = status,
			)
		}

		return collection.aggregate<MongoFlowExecution>(
			Aggregates.sort(Sorts.descending(MongoFlowLogs::flowId.name, MongoFlowLogs::creationDate.name)),
			Aggregates.group(
				MongoFlowLogs::flowId.mongoField,
				Accumulators.first(MongoFlowExecution::jobId.name, MongoFlowLogs::_id.mongoField),
				Accumulators.first(MongoFlowExecution::flowId.name, MongoFlowLogs::flowId.mongoField),
				Accumulators.first(MongoFlowExecution::status.name, MongoFlowLogs::status.mongoField),
				Accumulators.first(MongoFlowExecution::startDate.name, MongoFlowLogs::creationDate.mongoField),
			),
			Aggregates.lookup(
				/* from = */ FLOW_COLLECTION_NAME,
				// _id as the group makes the flowId be the _id
				/* localField = */ MongoFlowLogs::_id.name,
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
				Filters.eq(MongoFlowLogs::flowId.name, ObjectId(flowId)),
				if (startFrom != null) Filters.lt(MongoFlowLogs::_id.name, startFrom) else Filters.empty(),
			)
		)
			.sort(Sorts.descending(MongoFlowLogs::creationDate.name))
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
		collection.deleteMany(Filters.eq(MongoFlowLogs::flowId.name, ObjectId(flowId)))
	}
}
