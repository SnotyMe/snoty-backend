package me.snoty.backend.server.resources.wiring.credential

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialDefinitionRegistry

@OptIn(InternalSerializationApi::class)
fun JsonElement.convertToCredential(json: Json, credentialDefinitionRegistry: CredentialDefinitionRegistry, type: String): Credential {
	val deserializer = credentialDefinitionRegistry.lookupByType(type)
		.clazz.kotlin
		.serializerOrNull() ?: error("Unknown credential type '${type}'")
	return json.decodeFromJsonElement(deserializer, this)
}
