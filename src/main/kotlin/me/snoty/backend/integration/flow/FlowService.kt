package me.snoty.backend.integration.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.flow.model.FlowNode

interface FlowService {
	/**
	 * @return entrypoints of the flow (0-depth `next`-nodes)
	 */
	fun getFlowForNode(node: FlowNode): Flow<FlowNode>
}
