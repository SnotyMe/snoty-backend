package me.snoty.backend

import me.snoty.backend.build.DevBuildInfo
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.server.KtorServer
import me.snoty.backend.spi.DevManager

fun main() {
	val configLoader = ConfigLoaderImpl()
	val config = configLoader.loadConfig()

	val buildInfo = try {
		configLoader.loadBuildInfo()
	} catch (e: Exception) {
		// when ran without gradle, the build info is not available
		// it'll just default to dev build info in this case
		if (config.environment.isDev()) {
			println("Failed to load build info: ${e.message}")
			DevBuildInfo
		} else {
			throw e
		}
	}

	DevManager.runDevFunctions()

	KtorServer(config, buildInfo)
		.start(wait = true)
}
