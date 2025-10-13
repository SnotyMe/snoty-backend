package me.snoty.backend.authentication.oidc

import com.auth0.jwk.UrlJwkProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.authentication.User
import me.snoty.backend.config.Config
import me.snoty.backend.utils.UnauthorizedException
import me.snoty.backend.utils.parseAuthHeader
import me.snoty.backend.utils.respondStatus
import org.koin.core.annotation.Single
import java.net.URI

const val OIDC = "oidc"

@Single
open class OidcAuthenticationProvider(
	private val config: Config,
	private val oidcConfig: OidcConfig,
	private val httpClient: HttpClient,
) : AuthenticationProvider {
	private val logger = KotlinLogging.logger {}

	override suspend fun getUserById(userId: String): User? {
		logger.warn { "OIDC provider does not support getting User by ID" }
		return null
	}
	override suspend fun getRolesById(userId: String): List<Role> {
		logger.warn { "OIDC provider does not support getting Roles by ID" }
		return emptyList()
	}

	override suspend fun getRolesByToken(token: String): List<Role> {
		val response = httpClient.get(oidcConfig.userInfoUrl) {
			bearerAuth(token)
		}
			.body<JsonObject>()
		val remoteRoles = runCatching {
			response[oidcConfig.rolesClaim]?.jsonArray?.toList()?.map { it.jsonPrimitive.content }
		}
			.onFailure { exception ->
				logger.error(exception) { "Couldn't get user roles" }
			}
			.getOrNull()
			?: emptyList()

		return remoteRoles.mapToSnotyRoles()
	}

	protected fun List<String>.mapToSnotyRoles() = this.mapNotNull {
		oidcConfig.roleMappings.getOrDefault(it, null)
	}.map { Role(it) }

	override fun configureKtor(application: Application): Unit = with(application) {
		val oidcProvider = OAuthServerSettings.OAuth2ServerSettings(
			name = OIDC,
			authorizeUrl = oidcConfig.authUrl,
			accessTokenUrl = oidcConfig.tokenUrl,
			clientId = oidcConfig.clientId,
			clientSecret = oidcConfig.clientSecret,
			accessTokenRequiresBasicAuth = false,
			requestMethod = HttpMethod.Post,
			defaultScopes = listOf("openid")
		)

		install(Authentication) {
			oauth(OIDC) {
				client = httpClient
				providerLookup = { oidcProvider }
				urlProvider = { "${config.publicHost}/auth/callback" }
			}
			jwt("jwt-auth") {
				authHeader { call ->
					call.request.parseAuthHeader()
				}
				verifier(
					UrlJwkProvider(URI(oidcConfig.certUrl).toURL()),
					oidcConfig.issuerUrl
				)
				challenge { _, _ ->
					call.respondStatus(UnauthorizedException("JWT is invalid"))
				}
				validate { credential ->
					JWTPrincipal(credential.payload)
				}
			}
		}
		routing {
			authenticationResource(oidcConfig, httpClient, oidcProvider)
		}
	}
}
