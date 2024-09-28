package me.snoty.backend.utils

import me.snoty.backend.config.Config
import me.snoty.backend.config.Environment

/**
 * "Tenary for kotlin" - allows different handling when in dev mode
 * For ex., this can be used to show more verbose errors
 */
class IfDevPipeline<T>(val environment: Environment) {
	lateinit var ifDev: Environment.() -> T
}

infix fun <T> Config.ifDev(ifDev: Environment.() -> T): IfDevPipeline<T> = environment.ifDev(ifDev)

infix fun <T> Environment.ifDev(ifDev: Environment.() -> T): IfDevPipeline<T> =
	IfDevPipeline<T>(this).apply {
		this.ifDev = ifDev
	}

infix fun <T> IfDevPipeline<T>.otherwise(ifNotDev: Environment.() -> T): T =
	when {
		environment.isDev() -> ifDev.invoke(environment)
		else -> ifNotDev.invoke(environment)
	}
