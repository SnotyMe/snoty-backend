package me.snoty.backend.wiring.credential

import io.mockk.mockk
import kotlinx.serialization.Serializable

class TestCredentialRegistry : CredentialDefinitionRegistry {
	private val definitions: MutableMap<String, CredentialDefinition> = mutableMapOf(
		TestCredential.TYPE to TestCredential.DEFINITION,
	)

	fun register(type: String, definition: CredentialDefinition) {
		if (definition.type != type) {
			throw IllegalArgumentException("Credential definition type '${definition.type}' does not match the provided type '$type'")
		}
		definitions[type] = definition
	}

	fun registerTestCredential(type: String): String {
		register(type, TestCredential.DEFINITION.copy(type = type))
		return type
	}

	override fun lookupByType(credentialType: String): CredentialDefinition =
		definitions[credentialType]
			?: throw IllegalArgumentException("Credential type '$credentialType' is not registered")

	override fun lookupByClass(credentialClass: Class<out Credential>): CredentialDefinition =
		definitions.values.find { it.clazz == credentialClass }
			?: throw IllegalArgumentException("Credential class '${credentialClass.name}' is not registered")

	override fun getAll(): Collection<CredentialDefinition> = definitions.values
}

@Serializable
data class TestCredential(
	val password: String,
) : Credential() {
	companion object {
		const val TYPE = "test"

		val DEFINITION = CredentialDefinition(
			type = TYPE,
			displayName = "Test Credential",
			clazz = TestCredential::class.java,
			schema = mockk(),
		)
	}
}
