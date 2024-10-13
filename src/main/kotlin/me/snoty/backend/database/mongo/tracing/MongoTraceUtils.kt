package me.snoty.backend.database.mongo.tracing

import me.snoty.backend.database.mongo.tracing.MongoTracing.Companion.COMMANDS_WITH_COLLECTION_NAME
import me.snoty.backend.database.mongo.tracing.MongoTracing.Companion.getNonEmptyBsonString
import org.bson.BsonDocument

fun getCollectionName(command: BsonDocument, commandName: String): String? {
	if (COMMANDS_WITH_COLLECTION_NAME.contains(commandName)) {
		val collectionName = getNonEmptyBsonString(command[commandName])
		if (collectionName != null) {
			return collectionName
		}
	}
	// Some other commands, like getMore, have a field like {"collection": collectionName}.
	return getNonEmptyBsonString(command["collection"])
}
