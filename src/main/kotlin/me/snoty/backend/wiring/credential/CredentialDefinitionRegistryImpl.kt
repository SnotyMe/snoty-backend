package me.snoty.backend.wiring.credential

import me.snoty.backend.wiring.node.metadataJson
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
				displayName = it.displayName,
				clazz = it.clazz,
				schema = metadataJson.decodeFromString(it.schema),
			)

			if (registry.containsKey(definition.type) && registry[definition.type] != definition) {
				throw IllegalArgumentException("Credential type ${definition.type} is already registered")
			}

			registry[definition.type] = definition
		}
	}

	override fun lookupByType(credentialType: String) = registry[credentialType]
		?: throw IllegalArgumentException("Credential type $credentialType is not registered")

	override fun lookupByClass(credentialClass: Class<out Credential>) = registry.values.find { it.clazz == credentialClass }
		?: throw IllegalArgumentException("Credential class ${credentialClass.canonicalName} is not registered")

	override fun getAll() = registry.values.toList()
}
