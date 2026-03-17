package me.snoty.backend.authentication.oidc

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.*

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

fun Routing.authenticationResource(authConfig: OidcConfig, httpClient: HttpClient, provider: OAuthServerSettings.OAuth2ServerSettings) {
	val logger = KotlinLogging.logger {}
	route("/auth") {
		authenticate(OIDC) {
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
			val code = call.queryParameters["code"]
				?: return@post call.respondStatus(BadRequestException("Code is missing"))
			val redirectUrl = call.queryParameters["redirect_url"]
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
		}.describe {
			parameters {
				query("code") {
					this.schema = jsonSchema<String>()
					this.description = "The authorization code received from the OIDC provider after a successful authentication"
					this.required = true
				}
				query("redirect_url") {
					this.schema = jsonSchema<String>()
					this.description = "The redirect URL used in the authentication request, must match the one used in the initial request"
					this.required = true
				}
			}

			responses {
				HttpStatusCode.OK {
					schema = jsonSchema<OAuth2TokenResponse>()
				}
				HttpStatusCode.Unauthorized {
					description = "Failed to get token from OIDC provider, check the response body for more details"
				}
				HttpStatusCode.BadRequest {
					description = "Missing required query parameters"
				}
			}
		}

		post("/refresh") {
			val refreshToken = call.queryParameters["refresh_token"]
				?: return@post call.respondStatus(BadRequestException("Refresh token is missing"))
			val response = httpClient.submitForm(
				url = provider.accessTokenUrl,
				formParameters = parameters {
					set("grant_type", "refresh_token")
					set("refresh_token", refreshToken)
					set("client_id", authConfig.clientId)
					set("client_secret", authConfig.clientSecret)
				}
			)

			call.respondBytes(contentType = ContentType.Application.Json, bytes = response.bodyAsBytes())
		}.describe {
			parameters {
				query("refresh_token") {
					this.schema = jsonSchema<String>()
					this.description = "The refresh token received from the OIDC provider, used to obtain a new access token without requiring the user to re-authenticate"
					this.required = true
				}
			}

			responses {
				HttpStatusCode.OK {
					description = "Successfully obtained a new access token, the response body will contain the new token information"
					schema = jsonSchema<OAuth2TokenResponse>()
				}
			}
		}

		authenticate("jwt-auth") {
			post("/logout") {
				val idToken = call.queryParameters["id_token"] ?: return@post call.respondStatus(BadRequestException("ID token is missing"))
				val response = httpClient.get("${authConfig.logoutUrl}?id_token_hint=$idToken") {
					contentType(ContentType.Application.FormUrlEncoded)
					parameter("id_token_hint", idToken)
				}
				call.respondBytes(contentType = ContentType.Application.Json, bytes = response.bodyAsBytes())
			}.describe {
				parameters {
					query("id_token") {
						this.schema = jsonSchema<String>()
						this.description = "The ID token received from the OIDC provider, used to sign the user out of the OIDC provider as well"
						this.required = true
					}
				}
			}

			get("/userInfo") {
				call.respond(call.getUser())
			}

			get("/roles") {
				val roles = call.getUserRoles()

				call.respond(roles)
			}
		}
	}.describe {
		tag("auth")
	}
}
