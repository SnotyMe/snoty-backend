package me.snoty.backend.database.mongo

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.mockk.mockk
import me.snoty.backend.config.Config
import me.snoty.backend.config.MongoConfig
import me.snoty.backend.config.MongoConnectionConfig
import me.snoty.backend.database.mongo.migrations.provideMigrationsCodec
import me.snoty.backend.test.buildTestConfig
import me.snoty.backend.utils.bson.provideApiCodec
import me.snoty.backend.utils.bson.provideCodecRegistry
import me.snoty.integration.common.utils.bsonTypeClassMap
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

object MongoTest {
	@Container
	val mongoContainer = MongoDBContainer("mongo:8.0")
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
		return mongoStartup.createMongoClients(
			config.mongodb,
			provideCodecRegistry(provideMigrationsCodec(), provideApiCodec(bsonTypeClassMap())),
			"${name}_${javaClass.hashCode()}",
		).coroutinesDatabase
	}
}
