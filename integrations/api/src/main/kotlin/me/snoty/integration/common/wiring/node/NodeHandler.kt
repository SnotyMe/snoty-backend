package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import org.slf4j.Logger
import kotlin.reflect.KClass


/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 */
interface NodeHandler {
	val nodeHandlerContext: NodeHandlerContext
	val settingsClass: KClass<out NodeSettings>

	context(NodeHandlerContext, EmitNodeOutputContext)
	suspend fun process(logger: Logger, node: Node, input: IntermediateData)
}
