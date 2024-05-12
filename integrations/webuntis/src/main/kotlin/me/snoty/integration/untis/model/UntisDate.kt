package me.snoty.integration.untis.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.snoty.backend.utils.DateUtils
import java.time.LocalDate

@Serializable(with = UntisDate.Companion::class)
data class UntisDate(
	val date: LocalDate
) {
	companion object : KSerializer<UntisDate> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UntisDate", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: UntisDate) {
			encoder.encodeString(value.date.toString())
		}

		override fun deserialize(decoder: Decoder): UntisDate {
			val date = DateUtils.parseIsoDate(decoder.decodeString())
			return UntisDate(date)
		}
	}

	override fun toString(): String {
		return date.toString()
	}

	fun toLocalDate(): LocalDate {
		return date
	}
}
