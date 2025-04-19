package me.snoty.backend.wiring.flow.export

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document

@Serializable
data class ExportedFlow(
	val version: String,
	val templateName: String,
	val settings: WorkflowSettings,
	val nodes: List<ExportedNode>,
)

@Serializable
/**
 * Exported node with its settings and next nodes.
 * The ids are obfuscated by hashing them. This doubles as a prevention tactic of accidental mix-ups.
 */
data class ExportedNode(
	val id: String,
	val descriptor: NodeDescriptor,
	val settings: @Contextual Document,
	val next: List<String>,
)

@Serializable
data class CensoredField(val default: String?, val censored: Boolean = true)
