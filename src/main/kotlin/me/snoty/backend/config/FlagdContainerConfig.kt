package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigAlias

data class FlagdContainerConfig(
	@ConfigAlias("FLAGD_PORT")
	val port: Short
)
