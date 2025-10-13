package me.snoty.backend.database.mongo

import com.sksamuel.hoplite.ConfigLoaderBuilder
import me.snoty.backend.database.DatabaseAdapter

class MongoAdapter : DatabaseAdapter {
	override val supportedTypes = listOf(MONGODB)
	override val koinModule = mongoKoinModule

	override fun autoconfigure(configLoader: ConfigLoaderBuilder) = configLoader.autoconfigForMongo()
}
