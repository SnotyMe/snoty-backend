package me.snoty.backend.utils.bson

import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
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

fun Document.setRecursively(key: String, value: Any?) {
	val parts = key.split(".")
	if (parts.isEmpty()) throw IllegalArgumentException("Key must not be empty")

	val lastParent = getLastParent(parts)

	val lastPart = parts.last()
	when {
		// array index
		lastPart.matches("\\w+\\[\\d+]".toRegex())
			-> lastParent.setListIndex(lastPart, value)
		else -> lastParent[lastPart] = value
	}
}

private fun Document.setListIndex(specifier: String, value: Any?) {
	val key = specifier.substringBefore("[")
	val index = specifier.substringAfter("[").substringBefore("]").toInt()
	val list = when {
		containsKey(key) -> getList(key, Any::class.java)
		else -> {
			val newList = mutableListOf<Any?>()
			this[key] = newList
			newList
		}
	}

	if (index >= list.size) {
		// list containing nulls until the specified index
		val fillerList = List(index - list.size) { null }
		list.addAll(fillerList)
		list.add(value)
	} else {
		list[index] = value
	}
}

private fun Document.getLastParent(parts: List<String>): Document = parts
	.dropLast(1)
	.fold(this) { acc, part ->
		val next = acc[part] as? Document ?: Document()
		acc[part] = next
		next
	}
