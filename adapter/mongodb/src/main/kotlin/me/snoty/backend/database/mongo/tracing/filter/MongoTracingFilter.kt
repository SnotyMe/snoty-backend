package me.snoty.backend.database.mongo.tracing.filter

import com.mongodb.event.CommandStartedEvent

interface MongoTracingFilter {
	/**
	 * @return `true` if the command should be traced, `false` if it should be ignored, `null` if the filter does not care
	 */
	fun decide(event: CommandStartedEvent): Boolean?
}
