package me.snoty.backend.integration.untis.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

@Serializable(with = UnknownObject.Companion::class)
data class UnknownObject(val jsonString: String?) {
	companion object : KSerializer<UnknownObject> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UnknownObject", PrimitiveKind.STRING)

		@OptIn(ExperimentalSerializationApi::class)
		override fun serialize(encoder: Encoder, value: UnknownObject) {
			encoder.encodeNull()
		}

		override fun deserialize(decoder: Decoder): UnknownObject {
			return UnknownObject((decoder as? JsonDecoder)?.decodeJsonElement()?.toString())
		}
	}
}
