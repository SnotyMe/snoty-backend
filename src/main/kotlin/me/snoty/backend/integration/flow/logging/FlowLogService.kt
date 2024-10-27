package me.snoty.backend.integration.flow.logging

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
import me.snoty.backend.database.mongo.*
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.MongoWorkflow
import me.snoty.backend.integration.flow.execution.FlowFeatureFlags
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.integration.common.wiring.flow.*
import org.bson.codecs.pojo.annotations.BsonId
import org.koin.core.annotation.Single
import java.util.*
import java.util.concurrent.TimeUnit

interface FlowLogService {
	suspend fun create(jobId: String, flowId: NodeId, triggeredBy: FlowTriggerReason)
	suspend fun record(jobId: String, entry: NodeLogEntry)
	suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus)

	suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
	fun query(userId: UUID): Flow<EnumeratedFlowExecution>
	fun query(flowId: NodeId): Flow<FlowExecution>
}

internal data class FlowLogs(
	@BsonId
	/**
	 * The execution / job ID. Every flow can have multiple executions.
	 */
	val _id: String,
	val flowId: NodeId,
	val triggeredBy: FlowTriggerReason?,
	val creationDate: Instant,
	val status: FlowExecutionStatus? = null,
	val logs: List<NodeLogEntry>,
)

@Single
class MongoFlowLogService(mongoDB: MongoDatabase, featureFlags: FlowFeatureFlags) : FlowLogService {
	private val logger = KotlinLogging.logger {}
	private val collection = mongoDB.getCollection<FlowLogs>(FLOW_EXECUTION_COLLECTION_NAME)

	init {
		@OptIn(DelicateCoroutinesApi::class) // the indexes are non-critical and should just *eventually* be created
		GlobalScope.launch(Dispatchers.IO) {
			val expirationIndex = FlowLogs::creationDate.name

			collection.createIndexes(
				listOf(
					IndexModel(Filters.eq(FlowLogs::flowId.name, 1)),
					IndexModel(
						Filters.eq(FlowLogs::creationDate.name, 1),
						IndexOptions()
							.name(expirationIndex)
							.expireAfter(featureFlags.expirationSeconds, TimeUnit.SECONDS)
					),
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
			Aggregates.sort(Sorts.descending(FlowLogs::creationDate.name)),
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
				/* foreignField = */ MongoWorkflow::_id.name,
				/* as = */ flow,
			),
			Aggregates.match(Filters.elemMatch(flow, Filters.eq(MongoWorkflow::userId.name, userId))),
			Aggregations.project(
				Projections.exclude(flow),
			),
		).map { it.toFlowExecution() }
	}

	override fun query(flowId: NodeId): Flow<FlowExecution> =
		// TODO: paginate
		collection.find(Filters.eq(FlowLogs::flowId.name, flowId))
			.limit(15)
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
}
