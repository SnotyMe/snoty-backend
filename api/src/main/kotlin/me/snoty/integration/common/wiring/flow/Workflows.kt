package me.snoty.integration.common.wiring.flow

import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.FlowNode
import kotlin.uuid.Uuid

interface Workflow {
	val _id: String
	val name: String
	val userId: Uuid
}

/**
 * High-Level representation of a workflow, without any nodes.
 */
@Serializable
data class StandaloneWorkflow(
	override val _id: String,
	override val name: String,
	override val userId: Uuid,
) : Workflow

/**
 * High-Level representation of a workflow, with the involved nodes.
 */
 @Serializable
data class WorkflowWithNodes(
	override val _id: String,
	override val name: String,
	override val userId: Uuid,
	/**
	 * A list of all nodes.
	 * These nodes are normalized. Can include nodes not connected to anything.
	 */
	val nodes: List<FlowNode>,
) : Workflow
