package me.snoty.backend.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.snoty.backend.config.Config
import me.snoty.backend.config.OidcConfig
import me.snoty.backend.dev.auth.KeycloakConfigurer
import me.snoty.backend.dev.auth.REALM_NAME
import me.snoty.backend.utils.UnauthorizedException
import me.snoty.backend.test.assertErrorResponse
import me.snoty.backend.test.buildTestConfig
import me.snoty.backend.test.createAndLoginUser
import me.snoty.backend.test.ktorApplicationTest
import org.assertj.core.api.Assertions.*
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import java.util.*

class AuthenticationTest {
	companion object {
		@Container
		val keycloakContainer = KeycloakContainer()
		lateinit var config: Config

		@BeforeAll
		@JvmStatic
		fun setup() {
			keycloakContainer.start()
			val result = KeycloakConfigurer(keycloakContainer.keycloakAdminClient.realms(), REALM_NAME)
				.configure()
			config = buildTestConfig {
				authentication = OidcConfig(
					serverUrl = "${keycloakContainer.authServerUrl}/realms/$REALM_NAME",
					clientId = result.clientId,
					clientSecret = result.clientSecret
				)
			}
		}
	}

	@Test
	fun testUnauthorized() = ktorApplicationTest(config = config) {
		client.get("/userInfo").apply {
			assertEquals(HttpStatusCode.Unauthorized, status)
			assertThat(bodyAsText())
				.isNotEmpty()
			val body = JSONObject(bodyAsText())

			assertErrorResponse(body, UnauthorizedException("JWT is invalid"))
		}
	}

	@Test
	fun testAuthorized() = ktorApplicationTest(config = config) {
		val email = "authenticationtest.testauthorized@test.snoty.me"
		val user = keycloakContainer.keycloakAdminClient.realm(REALM_NAME)
			.createAndLoginUser(
				config.authentication,
				email = email
			)

		client.get("/userInfo") {
			header("Authorization", "Bearer ${user.accessToken}")
		}.apply {
			assertEquals(HttpStatusCode.OK, status)
			assertThat(bodyAsText()).isNotEmpty()
			val body = JSONObject(bodyAsText()).toMap()

			assertThat(body)
				.containsKeys("id", "email", "name")

			assertEquals(user.properties.email, body["email"])
			assertEquals(user.properties.username, body["name"])
			assertDoesNotThrow {
				UUID.fromString(body["id"] as String)
			}
		}
	}
}
