package me.snoty.integration.builtin.utils

import me.snoty.backend.utils.bson.encode
import me.snoty.backend.utils.bson.parseArray
import org.bson.Document
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonParseException

/**
 * Encodes all non-primitives in the [Document] using the provided [codecRegistry]
 */
fun Document.encodeObjects(codecRegistry: CodecRegistry) = Document(this.mapValues { (_, value) ->
	val packageName = value.javaClass.packageName
	if (packageName.startsWith("kotlin") || packageName.startsWith("java") || value::class == Document::class) value
	else codecRegistry.encode(value)
})

fun String.parseJson(codecRegistry: CodecRegistry, bsonTypeClassMap: BsonTypeClassMap): Any = trim().let {
	when ("${it.first()}${it.last()}") {
		"{}" -> Document.parse(it, codecRegistry.get(Document::class.java))
		"[]" -> parseArray(it, codecRegistry, bsonTypeClassMap)
		else -> throw JsonParseException("Invalid JSON: $it")
	}
}
