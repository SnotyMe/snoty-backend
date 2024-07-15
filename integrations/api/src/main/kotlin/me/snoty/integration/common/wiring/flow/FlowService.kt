package me.snoty.integration.common.wiring.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.RelationalFlowNode
import org.slf4j.Logger

interface FlowService {
	/**
	 * @return exactly one built [RelationalFlowNode] for the given [node] - the one item is equivalent to the input [node], just with the [RelationalFlowNode.next] populated
	 */
	fun getFlowForNode(node: Node): Flow<RelationalFlowNode>

	/**
	 * Runs the flow from a certain node with a certain input
	 *
	 * this DOES fetch the `next` nodes and runs them
	 */
	fun runFlow(jobId: String, logger: Logger, node: Node, input: IntermediateData): Flow<FlowLogEntry>
}
