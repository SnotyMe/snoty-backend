package me.snoty.backend.server.plugins

import com.auth0.jwk.UrlJwkProvider
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.backend.config.Config
import me.snoty.backend.server.plugins.security.authenticationResource
import me.snoty.backend.utils.UnauthorizedException
import me.snoty.backend.utils.parseAuthHeader
import me.snoty.backend.utils.respondStatus
import org.koin.core.annotation.Single
import java.net.URI

@Serializable
data class OAuth2TokenResponse(
	@SerialName("access_token")
	val accessToken: String,
	@SerialName("token_type")
	val tokenType: String,
	@SerialName("expires_in")
	val expiresIn: Long,
	@SerialName("refresh_token")
	val refreshToken: String?,
	// used to sign-out without redirects
	@SerialName("id_token")
	val idToken: String?,
)

fun Application.configureSecurity(config: Config, httpClient: HttpClient) {
	val authConfig = config.authentication
	val keycloakProvider = OAuthServerSettings.OAuth2ServerSettings(
		name = "keycloak",
		authorizeUrl = authConfig.authUrl,
		accessTokenUrl = authConfig.tokenUrl,
		clientId = authConfig.clientId,
		clientSecret = authConfig.clientSecret,
		accessTokenRequiresBasicAuth = false,
		requestMethod = HttpMethod.Post,
		defaultScopes = listOf("openid")
	)
	install(Authentication) {
		oauth("keycloak") {
			client = httpClient
			providerLookup = { keycloakProvider }
			urlProvider = { "${config.publicHost}/auth/callback" }
		}
		jwt("jwt-auth") {
			authHeader { call ->
				call.request.parseAuthHeader()
			}
			verifier(
				UrlJwkProvider(URI(authConfig.certUrl).toURL()),
				authConfig.issuerUrl
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
		authenticationResource(authConfig, httpClient, keycloakProvider)
	}
}

@Single
fun provideAuthConfig(config: Config) = config.authentication
