package me.snoty.integration.common.diff

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

data class Change<T1 : Any, T2 : Any>(
	val old: T1?,
	val new: T2?,
)

@Single
class ChangeCodec(private val codecRegistry: CodecRegistry, private val bsonTypeClassMap: BsonTypeClassMap) : Codec<Change<*, *>> {
	override fun getEncoderClass(): Class<Change<*, *>> = Change::class.java

	override fun encode(writer: BsonWriter, change: Change<*, *>, encoderContext: EncoderContext) = with(writer) {
		writeStartDocument()

		writeName("old")
		writeValue(change.old, encoderContext)
		writeName("new")
		writeValue(change.new, encoderContext)

		writeEndDocument()
	}

	private val nullCodec = BsonNullCodec()
	private fun <T : Any> BsonWriter.writeValue(value: T?, encoderContext: EncoderContext) = when (value) {
		null -> nullCodec.encode(this, null, encoderContext)
		else -> codecRegistry[value.javaClass].encode(this, value, encoderContext)
	}

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Change<*, *> = reader.run {
		readStartDocument()

		readName("old")
		val old = readValue(decoderContext)
		readName("new")
		val new = readValue(decoderContext)

		readEndDocument()

		Change(old, new)
	}

	private fun BsonReader.readValue(decoderContext: DecoderContext): Any? =
		codecRegistry[bsonTypeClassMap.get(currentBsonType)].decode(this, decoderContext)
}
