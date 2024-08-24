package me.snoty.backend.config

import me.snoty.backend.build.BuildInfo
import org.koin.core.annotation.Single

interface ConfigLoader {
	fun loadConfig(): Config
	fun loadBuildInfo(): BuildInfo
}

@Single
fun provideConfig(configLoader: ConfigLoader) = configLoader.loadConfig()
