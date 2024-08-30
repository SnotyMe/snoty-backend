package me.snoty.backend.dev.spi

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime


object DevManager {
	private val logger = KotlinLogging.logger {}

	private fun getCallables(): List<Runnable> {
		val loader: ServiceLoader<DevRunnable> = ServiceLoader.load(DevRunnable::class.java)

		val callables = loader.toList()

		logger.info {
			"Found ${callables.size} dev functions:\n${callables.joinToString("\n") { it.javaClass.typeName }}"
		}

		return callables
	}

	fun runDevFunctions() {
		// start all registered dev functions
		getCallables()
		.forEach {
			logger.debug { "Running dev function: ${it.javaClass.typeName}" }

			val time = measureTime {
				it.run()
			}
			val log: (() -> String) -> Unit = if (time > 1.seconds) logger::warn else logger::debug
			log {"Dev function ${it.javaClass.typeName} finished in $time" }
		}
	}
}
