package me.snoty.backend.spi

import me.snoty.integration.common.IntegrationFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object IntegrationRegistry {
	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	fun getIntegrationFactories(): List<IntegrationFactory> {
		val loader = ServiceLoader.load(IntegrationFactory::class.java)
		val integrations = loader.toList()

		if (integrations.isEmpty()) {
			logger.warn("No integration factories found!")
		} else {
			logger.debug("Located ${integrations.size} integration factories:\n${integrations.joinToString("\n") { it.javaClass.typeName }}")
		}

		return integrations
	}
}
