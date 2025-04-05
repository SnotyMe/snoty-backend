package me.snoty.backend.utils.bson

import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.uuid.Uuid

object UuidCodec : Codec<Uuid> {
	override fun getEncoderClass() = Uuid::class.java

	override fun encode(writer: BsonWriter, value: Uuid, encoderContext: EncoderContext?) {
		val binaryData = value.toByteArray()
		writer.writeBinaryData(BsonBinary(BsonBinarySubType.UUID_STANDARD, binaryData))
	}

	override fun decode(reader: BsonReader, decoderContext: DecoderContext?): Uuid {
		val subType = reader.peekBinarySubType()

		if (subType != BsonBinarySubType.UUID_LEGACY.value && subType != BsonBinarySubType.UUID_STANDARD.value) {
			throw BSONException("Unexpected BsonBinarySubType")
		}
		val bytes = reader.readBinaryData().data

		return Uuid.fromByteArray(bytes)
	}
}
