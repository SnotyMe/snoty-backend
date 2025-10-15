package me.snoty.backend.wiring.credential

import me.snoty.integration.common.model.metadata.ObjectSchema

data class CredentialDefinition(
	val type: String,
	val displayName: String,
	val clazz: Class<out Credential>,
	val schema: ObjectSchema,
)

annotation class RegisterCredential(val type: String, val displayName: String = "")

interface CredentialDefinitionContributor {
	val type: String
	val displayName: String
	val clazz: Class<out Credential>
	/**
	 * Serialized schema as a JSON string
	 */
	val schema: String
}
