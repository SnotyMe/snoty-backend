package me.snoty.backend.integration.flow.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.datetime.Instant
import me.snoty.backend.integration.config.flow.toNodeId
import me.snoty.backend.logging.toSLF4JLevel
import me.snoty.integration.common.wiring.flow.NodeLogEntry

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

	override fun append(eventObject: ILoggingEvent) {
		scope.launch {
			val message = eventObject.formattedMessage + (eventObject.throwableProxy?.message?.let {
				"\n$it"
			} ?: "")

			val entry = NodeLogEntry(
				timestamp = Instant.fromEpochMilliseconds(eventObject.timeStamp),
				level = eventObject.level.toSLF4JLevel(),
				message = message,
				node = eventObject.mdcPropertyMap["node.id"]?.toNodeId()
			)

			val flowId = eventObject.mdcPropertyMap["flow.id"]?.toNodeId() ?: return@launch

			flowLogService.record(flowId, entry)
		}
	}
}
