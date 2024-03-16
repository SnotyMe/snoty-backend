package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigAlias

enum class Environment {
	TEST,
	@ConfigAlias("DEV") DEVELOPMENT,
	@ConfigAlias("PROD") PRODUCTION;

	fun isDev() = this == DEVELOPMENT || this == TEST
}
