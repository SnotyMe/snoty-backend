package me.snoty.integration.common.diff

import me.snoty.backend.utils.resolveClassName
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

data class Change<T : Any>(val type: KClass<T>?, val old: T, val new: T) {
	constructor(old: T, new: T) : this(
		if (old.javaClass != new.javaClass)
			throw ClassCastException("Old (${old.javaClass}) and new (${new.javaClass}) values must be of the same type.")
		else
			old.javaClass.kotlin,
		old,
		new
	)
}

class ChangeCodec(private val codecRegistry: CodecRegistry) : Codec<Change<*>> {
	override fun getEncoderClass(): Class<Change<*>> = Change::class.java

	override fun encode(writer: BsonWriter, value: Change<*>, encoderContext: EncoderContext) {
		writer.apply {
			writeStartDocument()
			writeName("type")
			writeString(resolveClassName(value.type))
			writeName("old")
			writeValue(value.old, encoderContext)
			writeName("new")
			writeValue(value.new, encoderContext)
			writeEndDocument()
		}
	}

	private fun <T : Any> BsonWriter.writeValue(value: T, encoderContext: EncoderContext) {
		codecRegistry[value.javaClass].encode(this, value, encoderContext)
	}

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Change<*> {
		var type: Class<*>? = null
		var old: Any? = null
		var new: Any? = null

		reader.apply {
			readStartDocument()
			while (readBsonType() != BsonType.END_OF_DOCUMENT) {
				when (readName()) {
					"type" -> type = Class.forName(readString())
					"old" -> old = readValue(type, decoderContext)
					"new" -> new = readValue(type, decoderContext)
					else -> throw IllegalArgumentException("Unknown field: $currentName")
				}
			}
			readEndDocument()
		}

		@Suppress("UNCHECKED_CAST")
		return Change(type?.kotlin as KClass<Any>?, old!!, new!!)
	}


	private fun <T : Any> BsonReader.readValue(type: Class<T>?, decoderContext: DecoderContext): Any {
		return codecRegistry[type].decode(this, decoderContext)
	}
}
typealias Diff = Map<String, Change<*>>

typealias Fields = Document

fun Fields.checksum() = hashCode().toLong()
