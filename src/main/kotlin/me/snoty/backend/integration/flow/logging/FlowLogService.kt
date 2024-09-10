package me.snoty.backend.integration.flow.logging

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.bson.codecs.pojo.annotations.BsonId
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit

interface FlowLogService {
	suspend fun record(jobId: String, flowId: NodeId, entry: NodeLogEntry)

	suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
}

internal data class FlowLogs(
	@BsonId
	/**
	 * The execution / job ID. Every flow can have multiple executions.
	 */
	val _id: String,
	val creationDate: Instant,
	val flowId: NodeId,
	val logs: List<NodeLogEntry>,
)

@Single
class MongoFlowLogService(mongoDB: MongoDatabase, featureFlags: FeatureFlags) : FlowLogService {
	private val logger = KotlinLogging.logger {}
	private val collection = mongoDB.getCollection<FlowLogs>("$FLOW_COLLECTION_NAME.log")

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
							.expireAfter(featureFlags.get(featureFlags.flow_expirationSeconds), TimeUnit.SECONDS)
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

	override suspend fun record(jobId: String, flowId: NodeId, entry: NodeLogEntry) {
		collection.upsertOne(
			Filters.eq(FlowLogs::_id.name, jobId),
			Updates.combine(
				Updates.push(FlowLogs::logs.name, entry),
				Updates.setOnInsert(FlowLogs::flowId.name, flowId),
				Updates.setOnInsert(FlowLogs::creationDate.name, Clock.System.now()),
			)
		)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry> =
		collection.find(Filters.eq(FlowLogs::flowId.name, flowId))
			.toList()
			.flatMap { it.logs }
}
