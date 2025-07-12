package me.snoty.backend.database.mongo

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.mockk.mockk
import me.snoty.backend.database.mongo.migrations.provideMigrationsCodec
import me.snoty.backend.test.getClassNameFromBlock
import me.snoty.backend.utils.bson.provideApiCodec
import me.snoty.backend.utils.bson.provideCodecRegistry
import me.snoty.integration.common.utils.bsonTypeClassMap
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

object MongoTest {
	@Container
	val mongoContainer = MongoDBContainer("mongo:8.0")
	private val mongoStartup = MongoStartup(mockk(relaxed = true))
	val config: MongoConfig

	init {
		mongoContainer.start()
		config = MongoConfig(
			connection = MongoConnectionConfig.ConnectionString(mongoContainer.connectionString),
		)
	}

	fun getMongoClients(block: () -> Unit): MongoClients {
		val name = getClassNameFromBlock(block)
		return mongoStartup.createMongoClients(
			config,
			provideCodecRegistry(provideMigrationsCodec(), provideApiCodec(bsonTypeClassMap())),
			"${name}_${block.javaClass.hashCode()}",
		)
	}

	fun getMongoDatabase(block: () -> Unit): MongoDatabase = getMongoClients(block).coroutinesDatabase
}
