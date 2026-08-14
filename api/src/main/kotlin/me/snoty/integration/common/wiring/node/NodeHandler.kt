package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.NodeWithSettings
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput


/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 */
interface NodeHandler {
	context(_: NodeHandleContext)
	suspend fun process(node: NodeWithSettings, input: Collection<IntermediateData>): NodeOutput
}
