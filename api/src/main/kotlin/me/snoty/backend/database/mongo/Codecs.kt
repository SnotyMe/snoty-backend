package me.snoty.backend.database.mongo

import com.mongodb.MongoClientSettings
import kotlinx.datetime.*
import me.snoty.integration.common.utils.kotlinxSerializersModule
import org.bson.BsonDateTime
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.UuidRepresentation
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.ZoneOffset as JavaZoneOffset

class LocalDateTimeCodec : Codec<LocalDateTime> {
	override fun encode(writer: BsonWriter, value: LocalDateTime, encoderContext: EncoderContext) {
		writer.writeDateTime(value.toInstant(TimeZone.UTC).toEpochMilliseconds())
	}

	override fun getEncoderClass(): Class<LocalDateTime> = LocalDateTime::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): LocalDateTime {
		return fromEpochTimeMillis(reader.readDateTime())
	}
}

fun BsonDateTime.decode(): LocalDateTime = fromEpochTimeMillis(value)

fun fromEpochTimeMillis(dateTime: Long) =
	JavaLocalDateTime.ofEpochSecond(
		dateTime / 1000,
		(dateTime % 1000).toInt() * 1_000_000,
		JavaZoneOffset.UTC
	).toKotlinLocalDateTime()


class InstantCodec : Codec<Instant> {
	override fun encode(writer: BsonWriter, value: Instant, encoderContext: EncoderContext) {
		writer.writeDateTime(value.toEpochMilliseconds())
	}

	override fun getEncoderClass(): Class<Instant> = Instant::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Instant {
		return Instant.fromEpochMilliseconds(reader.readDateTime())
	}
}

fun apiCodecModule() =
	CodecRegistries.fromRegistries(
		CodecRegistries.fromCodecs(UuidCodec(UuidRepresentation.STANDARD), LocalDateTimeCodec(), InstantCodec()),
		CodecRegistries.fromProviders(KotlinSerializerCodecProvider(kotlinxSerializersModule)),
		MongoClientSettings.getDefaultCodecRegistry()
	)
