package me.snoty.backend.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import me.snoty.backend.authentication.keycloak.KeycloakAuthenticationProvider
import me.snoty.backend.authentication.keycloak.KeycloakConfig
import me.snoty.backend.authentication.keycloak.toOidcConfig
import me.snoty.backend.dev.authentication.KeycloakConfigurer
import me.snoty.backend.dev.authentication.REALM_NAME
import me.snoty.backend.test.TestConfig
import me.snoty.backend.test.assertErrorResponse
import me.snoty.backend.test.createAndLoginUser
import me.snoty.backend.test.ktorApplicationTest
import me.snoty.backend.utils.UnauthorizedException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import kotlin.uuid.Uuid

class AuthenticationTest {
	companion object {
		@Container
		val keycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:26.0")
		lateinit var keycloakConfig: KeycloakConfig
		val oidcConfig by lazy {
			keycloakConfig.toOidcConfig()
		}

		@BeforeAll
		@JvmStatic
		fun setup() {
			keycloakContainer.start()
			val result = KeycloakConfigurer(keycloakContainer.keycloakAdminClient.realms(), REALM_NAME)
				.configure()
			keycloakConfig = KeycloakConfig(
				baseUrl = keycloakContainer.authServerUrl,
				realm = REALM_NAME,
				clientId = result.clientId,
				clientSecret = result.clientSecret
			)
		}
	}

	private val keycloakAuthenticationProvider = KeycloakAuthenticationProvider(
		keycloakConfig = keycloakConfig,
		realm = keycloakContainer.keycloakAdminClient.realm(REALM_NAME),
		config = TestConfig,
		httpClient = HttpClient {}
	)
	private val configure: Application.() -> Unit = {
		keycloakAuthenticationProvider.configureKtor(this)
	}

	@Test
	fun `test unauthorized`() = ktorApplicationTest(configure = configure) {
		client.get("/auth/userInfo").apply {
			assertEquals(HttpStatusCode.Unauthorized, status)
			val body = JSONObject(bodyAsText())

			assertErrorResponse(body, UnauthorizedException("JWT is invalid"))
		}
	}

	@Test
	fun `test authorized`() = ktorApplicationTest(configure = configure) {
		val email = "authenticationtest.testauthorized@test.snoty.me"
		val user = keycloakContainer.keycloakAdminClient.realm(REALM_NAME)
			.createAndLoginUser(
				oidcConfig = oidcConfig,
				email = email
			)

		client.get("/auth/userInfo") {
			header("Authorization", "Bearer ${user.accessToken}")
		}.apply {
			assertEquals(HttpStatusCode.OK, status)
			val body = JSONObject(bodyAsText()).toMap()

			assertEquals(user.properties.email, body["email"])
			assertEquals(user.properties.username, body["name"])
			assertDoesNotThrow {
				Uuid.parse(body["id"] as String)
			}
		}
	}
}
