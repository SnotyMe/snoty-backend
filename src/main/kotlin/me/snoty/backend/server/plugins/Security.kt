package me.snoty.backend.server.plugins

import com.auth0.jwk.UrlJwkProvider
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.config.Config
import me.snoty.backend.utils.UnauthorizedException
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondStatus
import java.net.URI

fun Application.configureSecurity(config: Config) {
	val authConfig = config.authentication
	install(Authentication) {
		oauth("keycloak") {
			client = HttpClient(Apache)
			providerLookup = {
				OAuthServerSettings.OAuth2ServerSettings(
					name = "keycloak",
					authorizeUrl = "${authConfig.oidcUrl}/auth",
					accessTokenUrl = "${authConfig.oidcUrl}/token",
					clientId = authConfig.clientId,
					clientSecret = authConfig.clientSecret,
					accessTokenRequiresBasicAuth = false,
					requestMethod = HttpMethod.Post,
					defaultScopes = listOf("openid")
				)
			}
			urlProvider = {
				"${config.publicHost}/callback"
			}
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
		authenticate("keycloak") {
			get("/login") {}
			get("/callback") {
				val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
					?: return@get call.respondRedirect("/login")

				call.response.cookies.append("access_token", principal.accessToken)
				call.respondText(principal.accessToken)
			}
		}
		authenticate("jwt-auth") {
			get("/userInfo") {
				call.respond(call.getUser())
			}
		}
	}
}
