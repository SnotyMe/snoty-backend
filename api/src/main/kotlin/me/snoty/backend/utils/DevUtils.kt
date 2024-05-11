package me.snoty.backend.utils

import me.snoty.backend.config.Config
import me.snoty.backend.config.Environment

/**
 * "Tenary for kotlin" - allows different handling when in dev mode
 * For ex., this can be used to show more verbose errors
 */
class IfDevPipeline<T>(val config: Config, val environment: Environment = config.environment) {
	lateinit var ifDev: Config.() -> T
}

infix fun <T> Config.ifDev(ifDev: Config.() -> T): IfDevPipeline<T> {
	return IfDevPipeline<T>(this).apply {
		this.ifDev = ifDev
	}
}

infix fun <T> IfDevPipeline<T>.otherwise(ifNotDev: Config.() -> T): T {
	return if (environment.isDev()) {
		ifDev.invoke(config)
	} else {
		ifNotDev.invoke(config)
	}
}
