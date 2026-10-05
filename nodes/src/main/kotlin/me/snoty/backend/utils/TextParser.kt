package me.snoty.backend.utils

import me.snoty.backend.utils.bson.parseJson
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

enum class ParseFormat {
	TEXT,
	JSON,
}

@Single
class TextParser(
	private val codecRegistry: CodecRegistry,
	private val bsonTypeClassMap: BsonTypeClassMap,
) {
	fun parse(parseFormat: ParseFormat, serialized: String): Any {
		return when (parseFormat) {
			ParseFormat.TEXT -> serialized
			ParseFormat.JSON -> parseJson(serialized, codecRegistry, bsonTypeClassMap)
		}
	}
}
