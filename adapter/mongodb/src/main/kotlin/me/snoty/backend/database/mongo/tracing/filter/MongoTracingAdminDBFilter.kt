package me.snoty.backend.database.mongo.tracing.filter

import com.mongodb.event.CommandStartedEvent
import org.koin.core.annotation.Single

@Single
class MongoTracingAdminDBFilter : MongoTracingFilter {
	override fun decide(event: CommandStartedEvent) = when (event.databaseName) {
		"admin" -> false
		else -> null
	}
}
