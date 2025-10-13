package me.snoty.backend.wiring.credential

data class CredentialDefinition(
	val type: String,
	val clazz: Class<out Credential>,
)

annotation class RegisterCredential(val type: String)

interface CredentialDefinitionContributor {
	val type: String
	val clazz: Class<out Credential>
}
