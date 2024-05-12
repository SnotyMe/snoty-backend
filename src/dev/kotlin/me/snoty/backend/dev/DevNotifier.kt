package me.snoty.backend.dev

import me.snoty.backend.spi.DevRunnable

class DevNotifier : DevRunnable() {
	override fun run() {
		logger.info { "DevNotifier invoked!" }
	}
}
