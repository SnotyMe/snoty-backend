package me.snoty.integration.common.wiring.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.RelationalFlowNode
import org.slf4j.Logger

fun interface FlowRunner {
	fun execute(
		jobId: String,
		logger: Logger,
		node: RelationalFlowNode,
		input: IntermediateData
	): Flow<FlowLogEntry>
}
