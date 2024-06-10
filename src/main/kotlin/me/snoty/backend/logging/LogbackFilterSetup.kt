package me.snoty.backend.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory
import java.util.*

fun getLogbackFilters(): List<Filter<ILoggingEvent>> {
	@Suppress("UNCHECKED_CAST")
	return (ServiceLoader.load(Filter::class.java) as ServiceLoader<Filter<ILoggingEvent>>)
		.toList()
}

fun setupLogbackFilters() {
	val logger = KotlinLogging.logger {}
	val filters = getLogbackFilters()
	logger.info { "Found ${filters.size} logback filters" }
	val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
	loggerContext.loggerList.forEach {
		it.iteratorForAppenders().forEach { appender ->
			filters.forEach { filter ->
				appender.addFilter(filter)
			}
		}
	}
}
