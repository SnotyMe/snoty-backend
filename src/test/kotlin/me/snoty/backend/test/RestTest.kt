package me.snoty.backend.test

import io.ktor.server.routing.*
import io.ktor.server.testing.*
import me.snoty.backend.config.Config
import me.snoty.backend.server.plugins.configureRouting
import me.snoty.backend.server.plugins.configureSecurity
import me.snoty.backend.server.plugins.configureSerialization
import me.snoty.integration.common.BaseSnotyJson

fun ktorApplicationTest(
	config: Config = TestConfig,
	configure: Route.() -> Unit = {},
	block: suspend ApplicationTestBuilder.() -> Unit
) {
	testApplication {
		application {
			configureSerialization(BaseSnotyJson)
			configureSecurity(config)
			configureRouting(config)
		}

		routing {
			configure()
		}

		block()
	}
}
