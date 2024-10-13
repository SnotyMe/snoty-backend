package me.snoty.backend.database.mongo.tracing.filter

import ch.qos.logback.core.spi.FilterReply
import com.mongodb.event.CommandStartedEvent
import org.koin.core.annotation.Single

@Single
class MongoTracingAdminDBFilter : MongoTracingFilter {
	override fun decide(event: CommandStartedEvent): FilterReply = when (event.databaseName) {
		"admin" -> FilterReply.DENY
		else -> FilterReply.NEUTRAL
	}
}
