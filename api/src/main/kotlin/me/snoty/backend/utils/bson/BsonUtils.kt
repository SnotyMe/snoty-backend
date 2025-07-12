package me.snoty.backend.utils.bson

import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonReader
import org.bson.types.ObjectId
import kotlin.reflect.KClass

// the joys of mongodb
fun <T : Any> CodecRegistry.encode(value: T): Document {
	val document = BsonDocument()
	val writer = BsonDocumentWriter(document)
	@Suppress("UNCHECKED_CAST")
	val codec = get(value::class.java) as Codec<T>
	codec.encode(writer, value, EncoderContext.builder().build())

	val documentCodec = DocumentCodec(this)

	return documentCodec.decode(BsonDocumentReader(document), DecoderContext.builder().build())
}


// must be inlined to correctly create the context
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> CodecRegistry.decode(clazz: KClass<T>, document: Document): T {
	return get(clazz.java)
		.decode(
			BsonDocumentReader(document.toBsonDocument(BsonDocument::class.java, this)),
			DecoderContext.builder().build()
		)
}

fun Document.getIdAsString(): String? = when (val id = get("id")) {
	null -> null
	is String -> id
	is ObjectId -> id.toHexString()
	is Long -> id.toString()
	is Int -> id.toString()
	else -> throw IllegalArgumentException("Unsupported id type: $id")
}

fun Document.getOrPut(key: String, defaultValue: () -> Any): Any {
	return this[key] ?: defaultValue().also { this[key] = it }
}

fun parseArray(json: String, codecRegistry: CodecRegistry, bsonTypeClassMap: BsonTypeClassMap): List<Any> {
	val decoderContext = DecoderContext.builder().build()

	val reader = JsonReader(json)
	reader.readStartArray()

	val result = mutableListOf<Any>()
	while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
		val value = codecRegistry[bsonTypeClassMap.get(reader.currentBsonType)].decode(reader, decoderContext)
		result += value
	}

	reader.readEndArray()

	return result
}
