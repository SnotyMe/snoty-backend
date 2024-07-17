package me.snoty.integration.common.model.metadata

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.NodePosition

@Serializable
data class NodeMetadata(
	val displayName: String,
	val position: NodePosition,
	val settings: ObjectSchema,
	val input: ObjectSchema?,
	val output: ObjectSchema?
)

@Serializable
data class NodeField(
	val name: String,
	val type: String,
	val displayName: String,
	val description: String?,
	val hidden: Boolean,
	val censored: Boolean
)
