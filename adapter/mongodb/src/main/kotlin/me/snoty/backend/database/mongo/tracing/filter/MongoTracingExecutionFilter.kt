package me.snoty.backend.database.mongo.tracing.filter

import com.mongodb.event.CommandStartedEvent
import me.snoty.backend.database.mongo.FLOW_EXECUTION_COLLECTION_NAME
import me.snoty.backend.database.mongo.tracing.getCollectionName
import org.koin.core.annotation.Single

@Single
class MongoTracingExecutionFilter : MongoTracingFilter {
	override fun decide(event: CommandStartedEvent) = when {
		event.commandName == "update"
			&& getCollectionName(event.command, event.commandName) == FLOW_EXECUTION_COLLECTION_NAME
		-> false

		else -> null
	}
}
