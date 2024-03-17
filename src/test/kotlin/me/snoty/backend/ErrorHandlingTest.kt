package me.snoty.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import me.snoty.backend.config.Environment
import me.snoty.backend.test.TestConfig
import me.snoty.backend.test.ktorApplicationTest
import org.assertj.core.api.Assertions
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorHandlingTest {
	@Test
	fun testNotFound() = ktorApplicationTest {
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
	@Test
	fun testInternalServerErrorCatchAll_DevMode() =
		internalServerErrorCatchAllTest(Environment.DEVELOPMENT, "test", "test")

	@Test
	fun testInternalServerErrorCatchAll_ProdMode() =
		internalServerErrorCatchAllTest(Environment.PRODUCTION, "test", HttpStatusCode.InternalServerError.description)

	private fun internalServerErrorCatchAllTest(environment: Environment, errorMessage: String, expected: String) =
		ktorApplicationTest(config = TestConfig.copy(environment = environment)) {
			val path = "/internalerror"

			application {
				routing {
					get(path) {
						throw RuntimeException(errorMessage)
					}
				}
			}

			client.get(path).apply {
				assertEquals(HttpStatusCode.InternalServerError, status)
				Assertions.assertThat(bodyAsText())
					.isNotEmpty()
				val body = JSONObject(bodyAsText()).toMap()

				Assertions.assertThat(body)
					.containsEntry("code", 500)
					.containsEntry("message", expected)
			}
		}

}
