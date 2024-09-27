package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput


/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 */
interface NodeHandler {
	suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>): NodeOutput
}
