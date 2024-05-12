package me.snoty.backend.config

enum class Environment {
	TEST,
	DEVELOPMENT,
	PRODUCTION;

	fun isDev() = this == DEVELOPMENT || this == TEST
}
