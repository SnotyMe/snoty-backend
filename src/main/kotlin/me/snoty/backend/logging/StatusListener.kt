package me.snoty.backend.logging

import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.core.status.Status


/**
 * [Taken from StackOverflow](https://stackoverflow.com/a/61125380/10052779)
 * Required to make the initialization logs print *before* the application logs
 */
class StatusListener : OnConsoleStatusListener() {
	override fun addStatusEvent(status: Status) {
		if (status.level == LOG_LEVEL) {
			super.addStatusEvent(status)
		}
	}

	override fun start() {
		val statuses: List<Status> = context.statusManager.copyOfStatusList
		for (status in statuses) {
			if (status.level == LOG_LEVEL) {
				super.start()
			}
		}
	}

	companion object {
		private const val LOG_LEVEL: Int = Status.WARN
	}
}
