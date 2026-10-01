package me.snoty.backend.schema

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
@SerialName("SchemaFieldDetails")
@JsonSchema.Title("SchemaFieldDetails")
sealed class SchemaFieldDetails {
	@Serializable
	@SerialName("Enum")
	data class EnumDetails(
		val values: List<EnumConstant>
	) : SchemaFieldDetails() {
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
	) : SchemaFieldDetails()

	@Serializable
	@SerialName("Generic")
	data class GenericDetails(
		val genericType: String
	) : SchemaFieldDetails()

	@Serializable
	@SerialName("Object")
	data class ObjectDetails(
		val className: String,
		val schema: ObjectSchema,
	) : SchemaFieldDetails()

	@Serializable
	@SerialName("Collection")
	data class CollectionDetails(
		val elementDetails: SchemaFieldDetails?,
	) : SchemaFieldDetails()

	@Serializable
	@SerialName("Map")
	data class MapDetails(
		val keyDetails: SchemaFieldDetails?,
		val valueDetails: SchemaFieldDetails?,
	) : SchemaFieldDetails()

	@Serializable
	@SerialName("Credential")
	data class CredentialDetails(
		val credentialType: String,
		val schema: ObjectSchema,
	) : SchemaFieldDetails()

	@Serializable
	@SerialName("Duration")
	data class DurationDetails(
		val min: Duration?,
		val max: Duration?,
	) : SchemaFieldDetails()
}
