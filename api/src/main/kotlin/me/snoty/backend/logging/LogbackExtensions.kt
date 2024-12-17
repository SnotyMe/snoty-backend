package me.snoty.backend.logging

import ch.qos.logback.classic.Level as LogbackLevel
import org.slf4j.event.Level as SLF4JLevel

fun LogbackLevel.toSLF4JLevel()
	= when (this) {
		LogbackLevel.TRACE -> SLF4JLevel.TRACE
		LogbackLevel.DEBUG -> SLF4JLevel.DEBUG
		LogbackLevel.INFO -> SLF4JLevel.INFO
		LogbackLevel.WARN -> SLF4JLevel.WARN
		LogbackLevel.ERROR -> SLF4JLevel.ERROR
		else -> throw IllegalArgumentException("Unknown log level: $this")
	}
