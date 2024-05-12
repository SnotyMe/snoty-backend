package me.snoty.backend.spi

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.jvm.jvmName

abstract class DevRunnable : Runnable {
	// `this::class.jvmName` is to use the subclass name
	// see https://github.com/oshai/kotlin-logging/issues/91
	val logger = KotlinLogging.logger(this::class.jvmName)
}
