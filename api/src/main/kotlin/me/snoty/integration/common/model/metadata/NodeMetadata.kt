package me.snoty.integration.common.model.metadata

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.reflect.KClass

@Serializable
data class NodeMetadata(
	val displayName: String,
	val descriptor: NodeDescriptor,
	val position: NodePosition,
	val settings: ObjectSchema,
	@Transient
	val settingsClass: KClass<out NodeSettings> = throw NotImplementedError("Deserialization is not supported"),
	val input: ObjectSchema?,
	val output: ObjectSchema?
)

@Serializable
data class NodeField(
	val name: String,
	val type: String,
	val defaultValue: String?,
	val displayName: String,
	val description: String?,
	val hidden: Boolean,
	val censored: Boolean,
	val details: NodeFieldDetails?
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("_t")
sealed class NodeFieldDetails(
	val type: String
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
	data class PlaintextDetails @JvmOverloads constructor(
		val lines: Int,
		val defaultValue: String = "",
		val language: String? = null,
	) : NodeFieldDetails("Plaintext")

	@Serializable
	data class GenericDetails(
		val genericType: String
	) : NodeFieldDetails("Generic")

	@Serializable
	data class ObjectDetails(
		val className: String,
		val schema: ObjectSchema,
	) : NodeFieldDetails("Object")

	@Serializable
	data class CollectionDetails(
		val elementDetails: NodeFieldDetails?,
	) : NodeFieldDetails("Collection")

	@Serializable
	data class MapDetails(
		val keyDetails: NodeFieldDetails?,
		val valueDetails: NodeFieldDetails?,
	) : NodeFieldDetails("Map")
}
