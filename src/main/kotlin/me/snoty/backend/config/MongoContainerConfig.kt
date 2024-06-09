package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.Masked

data class MongoContainerConfig(
	@ConfigAlias("MONGO_INITDB_ROOT_USERNAME")
	val username: String?,
	@ConfigAlias("MONGO_INITDB_ROOT_PASSWORD")
	val password: Masked?,
	@ConfigAlias("MONGO_PORT")
	val port: Int = 27017
)
