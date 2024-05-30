package me.snoty.integration.common.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

typealias ConfigId = @Serializable(ConfigIdSerializer::class) ObjectId

object ConfigIdSerializer : KSerializer<ConfigId> {
	override val descriptor: SerialDescriptor = serialDescriptor<String>()

	override fun deserialize(decoder: Decoder): ConfigId {
		return ConfigId(decoder.decodeString())
	}

	override fun serialize(encoder: Encoder, value: ConfigId) {
		encoder.encodeString(value.toString())
	}
}
