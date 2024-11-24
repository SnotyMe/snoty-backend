package me.snoty.backend.wiring.flow.import

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document

@Serializable
data class ImportFlow(
	val name: String,
	val nodes: List<ImportNode>,
)

@Serializable
data class ImportNode(
	val id: String,
	val descriptor: NodeDescriptor,
	val settings: @Contextual Document,
	val next: List<String>,
)
