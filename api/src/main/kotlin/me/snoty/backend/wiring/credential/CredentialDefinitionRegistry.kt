package me.snoty.backend.wiring.credential

interface CredentialDefinitionRegistry {
	fun lookupByType(credentialType: String): CredentialDefinition
}
