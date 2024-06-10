package me.snoty.backend.dev.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class KtorFilter : Filter<ILoggingEvent>() {
	override fun decide(event: ILoggingEvent): FilterReply {
		if (event.message.startsWith("Watching") && event.message.endsWith("for changes.")) {
			return FilterReply.DENY
		}

		return FilterReply.NEUTRAL
	}
}
