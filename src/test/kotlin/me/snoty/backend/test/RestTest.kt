package me.snoty.backend.test

import io.ktor.server.testing.*
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
			Database.connect("jdbc:h2:mem:app", driver = "org.h2.Driver")
			val integrationManager = IntegrationManager(TestScheduler(), mockk()) {
				mockk()
			}
			addResources(buildInfo, integrationManager)
		}

		block()
	}
}
