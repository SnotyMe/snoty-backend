package me.snoty.integration.common.wiring.flow

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.FlowNode
import java.util.*

interface Workflow {
	val _id: String
	val name: String
	val userId: UUID
}

/**
 * High-Level representation of a workflow, without any nodes.
 */
@Serializable
data class StandaloneWorkflow(
	override val _id: String,
	override val name: String,
	@Contextual
	override val userId: UUID,
) : Workflow

/**
 * High-Level representation of a workflow, with the involved nodes.
 */
 @Serializable
data class WorkflowWithNodes(
	override val _id: String,
	override val name: String,
	@Contextual
	override val userId: UUID,
	/**
	 * A list of all nodes.
	 * These nodes are normalized. Can include nodes not connected to anything.
	 */
	val nodes: List<FlowNode>,
) : Workflow
