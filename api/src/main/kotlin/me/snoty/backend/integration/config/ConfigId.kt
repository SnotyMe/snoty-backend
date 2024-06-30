package me.snoty.backend.integration.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.snoty.backend.integration.config.flow.NodeId

typealias ConfigId = @Serializable(ConfigIdSerializer::class) NodeId

object ConfigIdSerializer : KSerializer<ConfigId> {
	override val descriptor: SerialDescriptor = serialDescriptor<String>()

	override fun deserialize(decoder: Decoder): ConfigId {
		return ConfigId(decoder.decodeString())
	}

	override fun serialize(encoder: Encoder, value: ConfigId) {
		encoder.encodeString(value.toString())
	}
}
