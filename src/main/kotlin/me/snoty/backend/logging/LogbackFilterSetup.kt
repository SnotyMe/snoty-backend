package me.snoty.backend.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

fun setupLogbackFilters(filters: List<Filter<ILoggingEvent>>) {
	val logger = KotlinLogging.logger {}
	logger.info { "Found ${filters.size} logback filters:\n${filters.joinToString("\n") { it.javaClass.typeName }}" }
	val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
	loggerContext.loggerList.forEach {
		it.iteratorForAppenders().forEach { appender ->
			filters.forEach { filter ->
				appender.addFilter(filter)
			}
		}
	}
}
