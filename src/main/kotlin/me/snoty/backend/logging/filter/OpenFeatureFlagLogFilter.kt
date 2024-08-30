package me.snoty.backend.logging.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import dev.openfeature.sdk.OpenFeatureClient
import org.koin.core.annotation.Single

@Single
class OpenFeatureFlagLogFilter : Filter<ILoggingEvent>() {
	override fun decide(event: ILoggingEvent): FilterReply = when {
		event.level.isGreaterOrEqual(Level.ERROR)
			&& event.loggerName == OpenFeatureClient::class.qualifiedName
			&& event.message.contains("Unable to correctly evaluate flag with key") -> FilterReply.DENY

		else -> FilterReply.NEUTRAL
	}
}
