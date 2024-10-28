package me.snoty.backend.server.plugins.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.config.OidcConfig
import me.snoty.backend.server.plugins.OAuth2TokenResponse
import me.snoty.backend.utils.*

fun Routing.authenticationResource(authConfig: OidcConfig, httpClient: HttpClient, provider: OAuthServerSettings.OAuth2ServerSettings) {
	val logger = KotlinLogging.logger {}
	route("/auth") {
		authenticate("keycloak") {
			get("/login") {}
			get("/callback") {
				val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
					?: return@get call.respondRedirect("/login")

				call.response.cookies.append(
					name = "access_token",
					value = principal.accessToken,
					path = "/",
				)
				call.respondText(principal.accessToken)
			}
		}
		post("/token") {
			val code = call.request.queryParameters["code"]
				?: return@post call.respondStatus(BadRequestException("Code is missing"))
			val redirectUrl = call.request.queryParameters["redirect_url"]
				?: return@post call.respondStatus(BadRequestException("Redirect URL is missing"))
			val response = httpClient.submitForm(
				url = provider.accessTokenUrl,
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
			get("/groups") {
				val groups = call.getUserGroups()

				call.respond(groups)
			}
		}
	}
}
