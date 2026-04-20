package me.snoty.backend.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.ContextAwareBase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

fun setupLogbackFilters(allFilters: List<ContextAwareBase>) {
	val logger = KotlinLogging.logger {}
	logger.info { "Found ${allFilters.size} logback filters:\n${allFilters.joinToString("\n") { it.javaClass.typeName }}" }

	val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

	allFilters
		.filterIsInstance<TurboFilter>()
		.forEach(loggerContext::addTurboFilter)

	loggerContext.loggerList.forEach {
		it.iteratorForAppenders().forEach { appender ->
			allFilters
				.filterIsInstance<Filter<ILoggingEvent>>()
				.forEach(appender::addFilter)
		}
	}
}
