package me.snoty.backend.integration.untis.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.snoty.backend.utils.DateTimeUtils
import java.time.LocalDateTime

@Serializable(with = UntisDateTime.Companion::class)
data class UntisDateTime(
	val dateTime: LocalDateTime
) {
	companion object : KSerializer<UntisDateTime> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UntisDateTime", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: UntisDateTime) {
			encoder.encodeString(value.dateTime.toString())
		}

		override fun deserialize(decoder: Decoder): UntisDateTime {
			val dateTime = DateTimeUtils.parseUntisFormat(decoder.decodeString())
			return UntisDateTime(dateTime)
		}
	}

	override fun toString(): String {
		return dateTime.toString()
	}

	fun toLocalDateTime(): LocalDateTime {
		return dateTime
	}
}
