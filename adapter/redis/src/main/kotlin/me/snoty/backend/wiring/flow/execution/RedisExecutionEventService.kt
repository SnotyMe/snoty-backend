package me.snoty.backend.wiring.flow.execution

import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.api.reactive.ChannelMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.serialization.json.Json
import me.snoty.backend.wiring.flow.execution.RedisFlowExecutionChannelUtils.flowChannelName
import me.snoty.backend.wiring.flow.execution.RedisFlowExecutionChannelUtils.userChannelName
import me.snoty.core.FlowId
import me.snoty.core.UserId
import org.koin.core.annotation.Single

@Single
class RedisExecutionEventService(
    redisClient: RedisClient,
    private val json: Json,
) : FlowExecutionEventService {
    private val logger = KotlinLogging.logger {}

    private val connection = redisClient.connectPubSub()
        .reactive()
    private val observeFlow = connection
        .observeChannels()
        .asFlow()

    override suspend fun provideFlowBus(flowId: FlowId): Flow<FlowExecutionEvent> {
        val channelName = flowChannelName(flowId)
        logger.trace { "Subscribing to flow channel $channelName" }
        connection.subscribe(channelName).awaitFirstOrNull()

        return observeFlow
            .filter { it.channel == channelName }
            .mapNotNull(::decodeEvent)
            .onCompletion {
                logger.trace { "Unsubscribing from flow channel $channelName" }
                connection.unsubscribe(channelName).awaitFirstOrNull()
            }
    }

    override suspend fun provideUserBus(userId: UserId): Flow<FlowExecutionEvent> {
        val channelName = userChannelName(userId)
        logger.trace { "Subscribing to user channel $channelName" }
        connection.subscribe(channelName).awaitFirstOrNull()

        return observeFlow
            .filter { it.channel == channelName }
            .mapNotNull(::decodeEvent)
            .onCompletion {
                logger.trace { "Unsubscribing from user channel $channelName" }
                connection.unsubscribe(channelName).awaitFirstOrNull()
            }
    }

    private fun decodeEvent(channelMessage: ChannelMessage<String, String>): FlowExecutionEvent? {
        return try {
            json.decodeFromString<FlowExecutionEvent>(channelMessage.message)
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode flow execution event: ${channelMessage.message}" }
            null
        }
    }

    override suspend fun offer(event: FlowExecutionEvent) {
        val eventJson = json.encodeToString(event)

        connection.publish(flowChannelName(event.flowId), eventJson).subscribe()
        if (event !is FlowExecutionEvent.FlowLogEvent) {
            // cross-flow logging is not supported, so we only publish user events for non-log events
            connection.publish(userChannelName(event.userId), eventJson).subscribe()
        }
    }
}
