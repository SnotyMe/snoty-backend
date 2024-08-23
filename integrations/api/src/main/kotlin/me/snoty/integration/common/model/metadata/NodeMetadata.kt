package me.snoty.integration.common.model.metadata

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.node.NodeDescriptor

@Serializable
data class NodeMetadata(
	val displayName: String,
	val descriptor: NodeDescriptor,
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
sealed class NodeFieldDetails(
	@Transient val valueType: String = throw NotImplementedError("Deserialization is not supported")
) {
	@Serializable
	data class EnumDetails(
		val values: List<EnumConstant>
	) : NodeFieldDetails("Enum") {
		@Serializable
		data class EnumConstant(
			val value: String,
			val displayName: String
		)
	}

	@Serializable
	data class PlaintextDetails(
		val lines: Int
	) : NodeFieldDetails("Plaintext")

	@Serializable
	data class GenericDetails(
		val genericType: String
	) : NodeFieldDetails("Generic")
}
