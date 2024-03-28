package me.snoty.backend.dev

import me.snoty.backend.spi.DevRunnable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DevNotifier : DevRunnable {
	private val logger: Logger = LoggerFactory.getLogger(DevNotifier::class.java)

	override fun run() {
		logger.info("DevNotifier invoked!")
	}
}
