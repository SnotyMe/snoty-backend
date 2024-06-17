package me.snoty.backend.integration.flow

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.integration.config.ConfigId
import me.snoty.backend.integration.config.flow.UserWorkflows

class MongoWorkflowHandler(mongoDB: MongoDatabase) : WorkflowHandler {
	val collection = mongoDB.getCollection<UserWorkflows>("flows")

	override fun <T> consume(source: ConfigId, entity: T) {
		collection.aggregate<>()
	}
}
