package me.snoty.backend.test

import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.testing.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.Config
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.server.plugins.addResources
import me.snoty.backend.server.plugins.configureRouting
import me.snoty.backend.server.plugins.configureSecurity
import me.snoty.backend.server.plugins.configureSerialization
import org.jetbrains.exposed.sql.Database

fun ktorApplicationTest(
	config: Config = TestConfig,
	buildInfo: BuildInfo = TestBuildInfo,
	block: suspend ApplicationTestBuilder.() -> Unit
) {
	testApplication {
		application {
			configureSerialization()
			configureSecurity(config)
			configureRouting(config)
			val db = Database.connect("jdbc:h2:mem:app", driver = "org.h2.Driver")
			val mongoDB = mockk<MongoDatabase>()
			every { mongoDB.getCollection<MongoCollection<*>>(any()) } returns mockk()
			every { mongoDB.getCollection<MongoCollection<*>>(any(), any()) } returns mockk()
			addResources(buildInfo, IntegrationManager(db, mongoDB, SimpleMeterRegistry(), TestScheduler()))
		}

		block()
	}
}
