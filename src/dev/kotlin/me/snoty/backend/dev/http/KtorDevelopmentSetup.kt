package me.snoty.backend.dev.http

import me.snoty.backend.dev.spi.DevRunnable

class KtorDevelopmentSetup : DevRunnable() {
	override fun run() {
		System.setProperty("io.ktor.development", "true")
	}
}
