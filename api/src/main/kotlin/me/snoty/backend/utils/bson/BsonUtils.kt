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

    var current: Any = this

    for (i in parts.indices) {
        val part = parts[i]
        val isLast = i == parts.lastIndex

	    current = when {
		    part.matches("^.*[^\\\\]\\[\\d+]$".toRegex()) -> current.handleList(part, isLast, value)
		    else -> current.handleRegularKey(part, isLast, value)
	    }
    }
}

private fun Any.handleRegularKey(part: String, isLast: Boolean, value: Any?): Any {
	if (this !is Document) {
		throw IllegalArgumentException("Expected a Document at '$part', but found: ${this::class.simpleName}")
	}

	return if (isLast) {
		this[part] = value
		this
	} else {
		this.getOrPut(part) { Document() }
	}
}

private fun Any.handleList(part: String, isLast: Boolean, value: Any?): Any {
	val key = part.substringBeforeLast("[")
	val index = part.substringAfterLast("[").substringBefore("]").toInt()

	@Suppress("UNCHECKED_CAST")
	val list = when (this) {
		is Document -> this.getOrPut(key) { mutableListOf<Any?>() }
		is MutableList<*> -> this
		else -> throw IllegalArgumentException("Expected a Document or List at '$part', but found: ${this::class.simpleName}")
	} as MutableList<Any?>

	while (index >= list.size) {
		list.add(null)
	}

	return if (isLast) {
		list[index] = value
		list
	} else {
		when {
			list[index] == null -> list[index] = Document()
			list[index] != null && list[index] !is Document ->
				throw IllegalArgumentException("Expected a Document at '$part[$index]', but found: ${list[index]!!::class.simpleName}")
		}

		list[index]!!
	}
}

private fun Document.getOrPut(key: String, defaultValue: () -> Any): Any {
    return this[key] ?: defaultValue().also { this[key] = it }
}
