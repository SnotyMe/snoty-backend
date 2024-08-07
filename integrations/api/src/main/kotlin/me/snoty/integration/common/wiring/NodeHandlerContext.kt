package me.snoty.integration.common.wiring

import io.opentelemetry.api.OpenTelemetry
import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.utils.calendar.CalendarService
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.codecs.configuration.CodecRegistry

internal data class NodeHandlerContext(
	val entityStateService: EntityStateService,
	val nodeService: NodeService,
	val flowService: FlowService,
	override val codecRegistry: CodecRegistry,
	val calendarService: CalendarService,
	override val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	val scheduler: Scheduler,
	override val openTelemetry: OpenTelemetry,
) : BaseNodeHandlerContext,
    CodecRegistryContext,
    OpenTelemetryContext

interface CodecRegistryContext : BaseNodeHandlerContext { val codecRegistry: CodecRegistry }
interface OpenTelemetryContext : BaseNodeHandlerContext { val openTelemetry: OpenTelemetry }
interface FetcherContext : BaseNodeHandlerContext { val entityStateService: EntityStateService }

interface BaseNodeHandlerContext {
	val intermediateDataMapperRegistry: IntermediateDataMapperRegistry
}

suspend operator fun <T : BaseNodeHandlerContext> T.invoke(block: suspend T.() -> Unit) = block()

internal typealias NodeContextBuilder<T> = (NodeDescriptor) -> T
