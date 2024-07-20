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
	val censored: Boolean,
	val details: NodeFieldDetails?
)

@Serializable
sealed class NodeFieldDetails {
	@Serializable
	data class EnumDetails(
		val values: List<EnumConstant>
	) : NodeFieldDetails() {
		@Serializable
		data class EnumConstant(
			val value: String,
			val displayName: String
		)
	}

	@Serializable
	data class GenericDetails(
		val genericType: String
	) : NodeFieldDetails()
}
