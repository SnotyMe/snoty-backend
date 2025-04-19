package me.snoty.backend.wiring.flow

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document

object FlowExportImportSchema {
	const val VERSION = "1.1"
}

@Serializable
data class ExportFlow(
	val version: String,
	val templateName: String,
	val settings: WorkflowSettings,
	val nodes: List<ExportNode>,
)

@Serializable
data class ImportFlow(
	val name: String,
	val settings: WorkflowSettings = WorkflowSettings(), // added in 1.1
	val nodes: List<ImportNode>,
)

@Serializable
/**
 * Exported node with its settings and next nodes.
 * The ids are obfuscated by hashing them. This doubles as a prevention tactic of accidental mix-ups.
 */
data class ExportNode(
	val id: String,
	val descriptor: NodeDescriptor,
	val settings: @Contextual Document,
	val next: List<String>,
)

@Serializable
data class CensoredField(val default: String?, val censored: Boolean = true)

@Serializable
data class ImportNode(
	val id: String,
	val descriptor: NodeDescriptor,
	val settings: @Contextual Document,
	val next: List<String>,
)
