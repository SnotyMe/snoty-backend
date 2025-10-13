package me.snoty.backend.wiring.credential

import org.koin.core.annotation.Single
import java.util.*

@Single
class CredentialDefinitionRegistryImpl : CredentialDefinitionRegistry {
	private val registry = mutableMapOf<String, CredentialDefinition>()

	init {
		val contributors = ServiceLoader.load(CredentialDefinitionContributor::class.java)

		contributors.map {
			val definition = CredentialDefinition(
				type = it.type,
				clazz = it.clazz,
			)

			if (registry.containsKey(definition.type) && registry[definition.type] != definition) {
				throw IllegalArgumentException("Credential type ${definition.type} is already registered")
			}

			registry[definition.type] = definition
		}
	}

	override fun lookupByType(credentialType: String) = registry[credentialType]
		?: throw IllegalArgumentException("Credential type $credentialType is not registered")
}
