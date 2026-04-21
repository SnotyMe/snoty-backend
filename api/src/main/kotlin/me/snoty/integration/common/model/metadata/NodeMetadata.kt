package me.snoty.integration.common.model.metadata

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.reflect.KClass
import me.snoty.integration.common.annotation.Icon as IconAnnotation

@Serializable
data class NodeMetadata(
	val displayName: String,
	val icon: Icon? = null,
	val descriptor: NodeDescriptor,
	val position: NodePosition,
	val settings: ObjectSchema,
	@Transient
	val settingsClass: KClass<out NodeSettings> = NodeSettings::class, // previously threw an error, but since we deserialize this AND remain compatible, we need to keep this
	val input: ObjectSchema?,
	val receiveEmptyInput: Boolean = false,
	val output: ObjectSchema?
)

@Serializable
data class Icon(
	val name: String,
	val color: String? = null,
) {
	companion object {
		fun of(icon: IconAnnotation) = Icon(
			name = icon.name,
			color = icon.color.takeIf(String::isNotEmpty)
		)
	}
}

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
@JsonClassDiscriminator("type")
sealed class NodeFieldDetails {
	@Serializable
	@SerialName("Enum")
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
	@SerialName("Plaintext")
	data class PlaintextDetails @JvmOverloads constructor(
		val lines: Int,
		@Deprecated("Use NodeField#defaultValue instead", ReplaceWith("defaultValue"))
		val defaultValue: String = "",
		val language: String? = null,
	) : NodeFieldDetails()

	@Serializable
	@SerialName("Generic")
	data class GenericDetails(
		val genericType: String
	) : NodeFieldDetails()

	@Serializable
	@SerialName("Object")
	data class ObjectDetails(
		val className: String,
		val schema: ObjectSchema,
	) : NodeFieldDetails()

	@Serializable
	@SerialName("Collection")
	data class CollectionDetails(
		val elementDetails: NodeFieldDetails?,
	) : NodeFieldDetails()

	@Serializable
	@SerialName("Map")
	data class MapDetails(
		val keyDetails: NodeFieldDetails?,
		val valueDetails: NodeFieldDetails?,
	) : NodeFieldDetails()

	@Serializable
	@SerialName("Credential")
	data class CredentialDetails(
		val credentialType: String,
		val schema: ObjectSchema,
	) : NodeFieldDetails()
}
