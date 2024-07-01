package me.snoty.integration.common.wiring.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.RelationalFlowNode

fun interface FlowRunner {
	fun execute(node: RelationalFlowNode, input: EdgeVertex): Flow<FlowOutput>
}
