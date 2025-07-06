package me.snoty.backend.integration.flow.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import me.snoty.backend.logging.toSLF4JLevel
import me.snoty.backend.observability.*
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.flow.execution.FlowExecutionEvent
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.slf4j.event.Level
import kotlin.time.Instant

private const val NAME = "NodeLogAppender"

class NodeLogAppender(
	private val flowExecutionService: FlowExecutionService,
	private val flowExecutionEventService: FlowExecutionEventService,
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
		val eventLevel = eventObject.level.toSLF4JLevel()
		if (eventLevel == Level.TRACE) return

		val appenderLevel = eventObject.mdcPropertyMap[APPENDER_LOG_LEVEL.key]?.let { Level.valueOf(it) } ?: Level.INFO
		if (eventLevel.toInt() < appenderLevel.toInt()) return

		val message = eventObject.formattedMessage + (eventObject.throwableProxy?.message?.let {
			"\n$it"
		} ?: "")

		val entry = NodeLogEntry(
			timestamp = Instant.fromEpochMilliseconds(eventObject.timeStamp),
			level = eventLevel,
			message = message,
			node = eventObject.mdcPropertyMap[NODE_ID.key]
		)

		val jobId = eventObject.mdcPropertyMap[JOB_ID.key]
			?: return logger.error { "No job ID found in ${eventObject.mdcPropertyMap} with msg='$message'" }

		scope.launch {
			flowExecutionEventService.offer(FlowExecutionEvent.FlowLogEvent(
				jobId = jobId,
				userId = eventObject.mdcPropertyMap[USER_ID.key]?.toUuid() ?: return@launch logger.error { "No User ID found in log entry with msg='$message'" },
				flowId = eventObject.mdcPropertyMap[FLOW_ID.key] ?: return@launch logger.error { "No Flow ID found in log entry with msg='$message'" },
				entry = entry,
			))

			flowExecutionService.record(jobId, entry)
		}
	}
}
