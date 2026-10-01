package me.snoty.backend.utils

import me.snoty.backend.utils.bson.parseJson
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

enum class SerializationFormat {
	TEXT,
	JSON,
}

@Single
class NodeSerializationUtils(
	private val codecRegistry: CodecRegistry,
	private val bsonTypeClassMap: BsonTypeClassMap,
) {
	fun deserialize(serializationFormat: SerializationFormat, serialized: String): Any {
		return when (serializationFormat) {
			SerializationFormat.TEXT -> serialized
			SerializationFormat.JSON -> parseJson(serialized, codecRegistry, bsonTypeClassMap)
		}
	}
}
