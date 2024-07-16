package me.snoty.integration.common.model

import kotlinx.serialization.Serializable

@Serializable
data class NodeMetadata(
	val displayName: String,
	val position: NodePosition,
	val settings: ObjectSchema,
	val input: ObjectSchema?,
	val output: ObjectSchema?
)

typealias ObjectSchema = List<NodeField>

@Serializable
data class NodeField(
	val name: String,
	val type: String,
	val hidden: Boolean,
	val censored: Boolean
)
