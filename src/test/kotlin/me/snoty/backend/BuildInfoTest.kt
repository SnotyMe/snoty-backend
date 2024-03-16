package me.snoty.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import me.snoty.backend.server.plugins.configureRouting
import me.snoty.backend.server.plugins.configureSerialization
import org.assertj.core.api.Assertions
import org.json.JSONObject
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildInfoTest : KoinTest {
	@Test
	fun testBuildInfoEndpoint() {
		testApplication {
			application {
				configureSerialization()
				configureRouting()
			}
			client.get("/info").apply {
				assertEquals(HttpStatusCode.OK, status)
				Assertions.assertThat(bodyAsText())
					.isNotEmpty()
				val body = JSONObject(bodyAsText()).toMap()

				Assertions.assertThat(body)
					.containsKeys("gitBranch", "gitCommit", "buildDate", "version", "application")
			}
		}
	}
}
