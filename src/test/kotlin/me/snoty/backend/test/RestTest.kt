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
			val integrationManager = IntegrationManager(TestScheduler(), mockk(), mockk()) {
				mockk()
			}
			addResources(buildInfo, integrationManager)
		}

		block()
	}
}
