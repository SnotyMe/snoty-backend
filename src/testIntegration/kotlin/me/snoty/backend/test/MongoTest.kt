package me.snoty.backend.test

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.config.Config
import me.snoty.backend.config.MongoConfig
import me.snoty.backend.database.mongo.createMongoClients
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

object MongoTest {
	@Container
	val mongoContainer = MongoDBContainer("mongo:8.0-rc")
	val config: Config

	init {
		mongoContainer.start()
		config = buildTestConfig {
			mongodb = MongoConfig(mongoContainer.connectionString)
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun getDatabase(): MongoDatabase {
		return createMongoClients(config.mongodb, "${javaClass.simpleName}_${javaClass.hashCode()}").first
	}
}
