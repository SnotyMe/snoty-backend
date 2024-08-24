package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import org.slf4j.Logger


/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 */
interface NodeHandler {
	val metadata: NodeMetadata

	context(NodeHandleContext)
	suspend fun process(logger: Logger, node: Node, input: IntermediateData)
}
