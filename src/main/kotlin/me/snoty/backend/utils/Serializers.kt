package me.snoty.backend.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.slf4j.event.Level

class Slf4jLevelSerializer : KSerializer<Level> {
	override val descriptor = PrimitiveSerialDescriptor(Level::class.qualifiedName!!, PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: Level) = encoder.encodeString(value.name)

	override fun deserialize(decoder: Decoder): Level = Level.valueOf(decoder.decodeString())
}
