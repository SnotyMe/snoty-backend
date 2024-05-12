package me.snoty.backend.spi

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.integration.common.IntegrationFactory
import java.util.*

object IntegrationRegistry {
	private val logger = KotlinLogging.logger {}

	fun getIntegrationFactories(): List<IntegrationFactory> {
		val loader = ServiceLoader.load(IntegrationFactory::class.java)
		val integrations = loader.toList()

		if (integrations.isEmpty()) {
			logger.warn { "No integration factories found!" }
		} else {
			logger.debug {
				"Located ${integrations.size} integration factories:\n${integrations.joinToString("\n") { it.javaClass.typeName }}"
			}
		}

		return integrations
	}
}
