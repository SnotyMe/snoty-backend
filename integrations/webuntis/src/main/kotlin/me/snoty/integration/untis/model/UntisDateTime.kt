package me.snoty.integration.untis.model

import kotlinx.datetime.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.snoty.backend.utils.DateTimeUtils
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

@Serializable(with = UntisDateTime.Companion::class)
data class UntisDateTime(
	val dateTime: Instant
) {
	companion object : KSerializer<UntisDateTime>, Codec<UntisDateTime> {
		// kotlinx.serialization
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UntisDateTime", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: UntisDateTime) {
			encoder.encodeString(value.dateTime.toString())
		}

		override fun deserialize(decoder: Decoder): UntisDateTime {
			val dateTime = DateTimeUtils.parseIsoDateTime(decoder.decodeString())
			return UntisDateTime(dateTime.toInstant(ZoneOffset.UTC).toKotlinInstant())
		}

		fun fromString(dateTime: String): UntisDateTime {
			return UntisDateTime(DateTimeUtils.parseIsoDateTime(dateTime).toInstant(ZoneOffset.UTC).toKotlinInstant())
		}

		fun fromDate(date: Date): UntisDateTime {
			return UntisDateTime(Instant.fromEpochMilliseconds(date.time))
		}

		// BSON
		override fun getEncoderClass(): Class<UntisDateTime> = UntisDateTime::class.java

		override fun encode(writer: BsonWriter, value: UntisDateTime, encoderContext: EncoderContext)
			= writer.writeDateTime(value.dateTime.toEpochMilliseconds())

		override fun decode(reader: BsonReader, decoderContext: DecoderContext): UntisDateTime
			= UntisDateTime(Instant.fromEpochMilliseconds(reader.readDateTime()))
	}

	override fun toString(): String {
		return dateTime.toString()
	}

	fun toLocalDateTime(): LocalDateTime {
		return dateTime.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
	}

	override fun equals(other: Any?): Boolean {
		if (super.equals(other)) return true

		if (other as? UntisDateTime != null) {
			// optionally compare seconds to fix flaky tests
			return other.dateTime.epochSeconds == dateTime.epochSeconds
		}

		return false
	}

	override fun hashCode(): Int = super.hashCode()
}
