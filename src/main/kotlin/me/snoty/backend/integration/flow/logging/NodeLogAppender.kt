package me.snoty.backend.integration.flow.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.datetime.Instant
import me.snoty.backend.integration.config.flow.toNodeId
import me.snoty.backend.logging.toSLF4JLevel
import me.snoty.backend.observability.APPENDER_LOG_LEVEL
import me.snoty.backend.observability.FLOW_ID
import me.snoty.backend.observability.JOB_ID
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.slf4j.event.Level

private const val NAME = "NodeLogAppender"

class NodeLogAppender(
	private val flowLogService: FlowLogService
) : AppenderBase<ILoggingEvent>() {
	init {
		setName(NAME)
	}

	@OptIn(DelicateCoroutinesApi::class)
	private val dispatcher = newSingleThreadContext(NAME)
	private val scope = CoroutineScope(dispatcher)

	// won't loop as the appender isn't installed
	val logger = KotlinLogging.logger {}

	override fun append(eventObject: ILoggingEvent) {
		scope.launch {
			val eventLevel = eventObject.level.toSLF4JLevel()
			val appenderLevel = eventObject.mdcPropertyMap[APPENDER_LOG_LEVEL.key]?.let { Level.valueOf(it) } ?: Level.INFO
			if (eventLevel.toInt() < appenderLevel.toInt()) {
				return@launch
			}

			val message = eventObject.formattedMessage + (eventObject.throwableProxy?.message?.let {
				"\n$it"
			} ?: "")

			val entry = NodeLogEntry(
				timestamp = Instant.fromEpochMilliseconds(eventObject.timeStamp),
				level = eventLevel,
				message = message,
				node = eventObject.mdcPropertyMap["node.id"]?.toNodeId()
			)

			val jobId = eventObject.mdcPropertyMap[JOB_ID.key]
				?: return@launch logger.warn { "No job ID found in log entry with msg='$message'" }
			val flowId = eventObject.mdcPropertyMap[FLOW_ID.key]?.toNodeId()
				?: return@launch logger.warn { "No flow ID found in log entry with msg='$message'" }

			flowLogService.record(jobId, flowId, entry)
		}
	}
}
