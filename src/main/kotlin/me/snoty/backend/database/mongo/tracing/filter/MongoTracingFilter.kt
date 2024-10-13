package me.snoty.backend.database.mongo.tracing.filter

import ch.qos.logback.core.spi.FilterReply
import com.mongodb.event.CommandStartedEvent

interface MongoTracingFilter {
	fun decide(event: CommandStartedEvent): FilterReply
}
