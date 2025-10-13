package me.snoty.backend.wiring.credential

interface CredentialDefinitionRegistry {
	fun lookupByType(credentialType: String): CredentialDefinition
	fun lookupByClass(credentialClass: Class<out Credential>): CredentialDefinition
	fun getAll(): Collection<CredentialDefinition>
}
