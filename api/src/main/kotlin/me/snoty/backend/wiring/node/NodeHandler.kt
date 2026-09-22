package me.snoty.backend.wiring.node

import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.core.node.NodeWithSettings


/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 */
interface NodeHandler {
	context(_: NodeHandleContext)
	suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput
}
