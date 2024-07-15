package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import org.slf4j.Logger
import kotlin.reflect.KClass


/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 *
 * One [NodeHandler] can only handle one type of node.
 */
interface NodeHandler {
	val nodeHandlerContext: NodeHandlerContext

	/**
	 * Process the current node, **not** its children.
	 * Can emit multiple results
	 */
	context(NodeHandlerContext, EmitNodeOutputContext)
	suspend fun process(logger: Logger, node: Node, input: IntermediateData)

	/**
	 * Where the node is placed.
	 * Start nodes cannot have incoming edges.
	 * End nodes cannot have outgoing edges.
	 *
	 * Mostly useful to build a database query for all start nodes.
	 */
	val position: NodePosition
	val settingsClass: KClass<out NodeSettings>
}
