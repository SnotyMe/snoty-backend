package me.snoty.backend.config

import me.snoty.backend.build.BuildInfo

interface ConfigLoader {
	fun loadConfig(): Config
	fun loadBuildInfo(): BuildInfo
}
