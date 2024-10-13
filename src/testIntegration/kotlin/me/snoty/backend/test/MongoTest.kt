package me.snoty.backend.test

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.mockk.mockk
import me.snoty.backend.config.Config
import me.snoty.backend.config.MongoConfig
import me.snoty.backend.config.MongoConnectionConfig
import me.snoty.backend.database.mongo.MongoStartup
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

object MongoTest {
	@Container
	val mongoContainer = MongoDBContainer("mongo:8.0-rc")
	private val mongoStartup = MongoStartup(mockk(relaxed = true))
	val config: Config

	init {
		mongoContainer.start()
		config = buildTestConfig {
			mongodb = MongoConfig(
				connection = MongoConnectionConfig.ConnectionString(mongoContainer.connectionString),
			)
		}
	}

	fun getMongoDatabase(block: () -> Unit): MongoDatabase {
		val javaClass = block.javaClass
		var name = javaClass.name
		name = when {
			name.contains("Kt$") -> name.substringBefore("Kt$")
			name.contains("$") -> name.substringBefore("$")
			else -> name
		}.substringAfterLast(".")
		return mongoStartup.createMongoClients(config.mongodb, "${name}_${javaClass.hashCode()}").coroutinesDatabase
	}
}
