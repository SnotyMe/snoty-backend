package me.snoty.backend.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.filter.Filter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

fun setupLogbackFilters(filters: List<Filter<ILoggingEvent>>, turboFilters: List<TurboFilter>) {
	val logger = KotlinLogging.logger {}
	val allFilters = filters + turboFilters
	logger.info { "Found ${allFilters.size} logback filters:\n${allFilters.joinToString("\n") { it.javaClass.typeName }}" }

	val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

	turboFilters.forEach { filter ->
		loggerContext.addTurboFilter(filter)
	}

	loggerContext.loggerList.forEach {
		it.iteratorForAppenders().forEach { appender ->
			filters.forEach { filter ->
				appender.addFilter(filter)
			}
		}
	}
}
