package me.snoty.backend.server.plugins

import com.auth0.jwk.UrlJwkProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.backend.config.Config
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.UnauthorizedException
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.SnotyJson
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
)

fun Application.configureSecurity(config: Config) {
	val logger = KotlinLogging.logger {}
	val authConfig = config.authentication
	val httpClient = HttpClient {
		install(ContentNegotiation) {
			json(SnotyJson)
		}
	}
	val keycloakProvider = OAuthServerSettings.OAuth2ServerSettings(
		name = "keycloak",
		authorizeUrl = "${authConfig.oidcUrl}/auth",
		accessTokenUrl = "${authConfig.oidcUrl}/token",
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
				call.request.parseAuthorizationHeader()
					// optionally load from cookies
					?: parseAuthorizationHeader("Bearer ${call.request.cookies["access_token"]}")
			}
			verifier(
				UrlJwkProvider(URI("${authConfig.oidcUrl}/certs").toURL()),
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
		route("/auth") {
			authenticate("keycloak") {
				get("/login") {}
				get("/callback") {
					val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
						?: return@get call.respondRedirect("/login")

					call.response.cookies.append("access_token", principal.accessToken)
					call.respondText(principal.accessToken)
				}
			}
			post("/token") {
				val code = call.request.queryParameters["code"]
					?: return@post call.respondStatus(BadRequestException("Code is missing"))
				val redirectUrl = call.request.queryParameters["redirect_url"]
					?: return@post call.respondStatus(BadRequestException("Redirect URL is missing"))
				val response = httpClient.submitForm(
					url = keycloakProvider.accessTokenUrl,
					formParameters = parameters {
						set("grant_type", "authorization_code")
						set("code", code)
						set("redirect_uri", redirectUrl)
						set("client_id", authConfig.clientId)
						set("client_secret", authConfig.clientSecret)
					}
				) {
					header(HttpHeaders.Accept, ContentType.Application.Json.toString())
				}
				if (!response.status.isSuccess()) {
					// TODO: add error to trace
					val message = response.bodyAsText()
					logger.debug {
						"Failed to get token: ${response.status} $message"
					}
					return@post call.respondStatus(UnauthorizedException("Failed to get token"))
				}
				val token = response.body<OAuth2TokenResponse>()
				call.response.cookies.append("access_token", token.accessToken)
				if (token.refreshToken != null)
					call.response.cookies.append("refresh_token", token.refreshToken)
				call.respond(token)
			}
			authenticate("jwt-auth") {
				get("/userInfo") {
					call.respond(call.getUser())
				}
			}
		}
	}
}
