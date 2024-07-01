package me.snoty.integration.common

import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.utils.calendar.CalendarService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.codecs.configuration.CodecRegistry

data class NodeContext(
	val entityStateService: EntityStateService,
	val nodeService: NodeService,
	val flowService: FlowService,
	val codecRegistry: CodecRegistry,
	val calendarService: CalendarService,
	val scheduler: Scheduler
)

typealias NodeContextBuilder = (NodeDescriptor) -> NodeContext
