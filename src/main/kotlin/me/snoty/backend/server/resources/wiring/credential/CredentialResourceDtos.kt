package me.snoty.backend.server.resources.wiring.credential

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialDefinitionRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.jvm.jvmName

@Serializable
data class CredentialCreateDto(
	val type: String,
	val name: String,
	@Contextual
	val data: Credential,
)

@Single
class CredentialResourceDtoSerializer(
	private val credentialDefinitionRegistry: CredentialDefinitionRegistry,
) : KSerializer<CredentialCreateDto> {
	override val descriptor = buildClassSerialDescriptor(CredentialCreateDto::class.jvmName) {
		element<String>("type")
		element<String>("name")
		element<Any>("data")
	}

	override fun serialize(encoder: Encoder, value: CredentialCreateDto) = throw NotImplementedError()

	@OptIn(InternalSerializationApi::class)
	override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
		var type: String? = null
		var name: String? = null
		var data: Credential? = null

		while (true) {
			when (decodeElementIndex(descriptor)) {
				0 -> type = decodeStringElement(descriptor, 0)
				1 -> name = decodeStringElement(descriptor, 1)
				2 -> data = decodeSerializableElement(
					descriptor,
					2,
					credentialDefinitionRegistry.lookupByType(type!!).clazz.kotlin.serializerOrNull() ?: error("Unknown credential type '$type'")
				)
				else -> break
			}
		}

		CredentialCreateDto(
			type ?: error("Missing 'type'"),
			name ?: error("Missing 'name'"),
			data ?: error("Missing 'data'"),
		)
	}
}
