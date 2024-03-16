package me.snoty.backend

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.mockk
import me.snoty.backend.config.Config
import me.snoty.backend.config.DatabaseConfig
import me.snoty.backend.config.Environment
import me.snoty.backend.server.plugins.configureRouting
import me.snoty.backend.server.plugins.configureSerialization
import org.assertj.core.api.Assertions
import org.json.JSONObject
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorHandlingTest {
	@Test
	fun testNotFound() = testApplication {
		val appModule = module {
			single<Config> {
				Config(
					port = 8080,
					environment = Environment.TEST,
					database = DatabaseConfig(mockk<HikariDataSource>())
				)
			}
		}

		koinApplication {
			modules(appModule)
		}

		application {
			configureSerialization()
			configureRouting()
		}
		client.get("/unknownroute").apply {
			assertEquals(HttpStatusCode.NotFound, status)
			Assertions.assertThat(bodyAsText())
				.isNotEmpty()

			val body = JSONObject(bodyAsText()).toMap()

			Assertions.assertThat(body)
				.containsEntry("code", 404)
				.containsEntry("message", "Not Found")
		}
	}
}
