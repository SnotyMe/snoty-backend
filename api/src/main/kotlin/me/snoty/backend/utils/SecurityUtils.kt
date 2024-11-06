package me.snoty.backend.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.snoty.backend.User
import me.snoty.backend.config.OidcConfig
import me.snoty.backend.config.getReverseMapping
import me.snoty.backend.server.koin.get
import java.util.*

fun ApplicationRequest.parseAuthHeader() =
	call.request.parseAuthorizationHeader()
		// optionally load from cookies
		?: parseAuthorizationHeader("Bearer ${call.request.cookies["access_token"]}")

fun ApplicationCall.getUser(): User =
	getUserOrNull() ?: throw UnauthorizedException("User not authenticated")

fun ApplicationCall.getUserOrNull(): User? {
	val principal = authentication.principal<JWTPrincipal>() ?: return null
	val claims = principal.payload.claims

	return User(
		id = claims["sub"]?.`as`(UUID::class.java) ?: NULL_UUID,
		name = claims["preferred_username"]?.asString() ?: "unknown",
		email = claims["email"]?.asString() ?: "unknown",
	)
}

/**
 * Gets mapped user groups
 */
suspend fun ApplicationCall.getUserGroups(): List<String> {
	val oidcConfig: OidcConfig = get()

	val token = request.parseAuthHeader() as? HttpAuthHeader.Single
		?: throw UnauthorizedException("Invalid token")
	val groups = getGroups(token.blob)
	return groups.mapNotNull {
		oidcConfig.groupMappings.getReverseMapping(it)
	}
}

suspend fun ApplicationCall.requireAnyGroup(vararg groups: String) {
	val userGroups = getUserGroups()

	if (!groups.any(userGroups::contains)) {
		throw ForbiddenException("Missing groups '$groups'")
	}
}

private suspend fun ApplicationCall.getGroups(token: String): List<String> {
	val httpClient: HttpClient = get()
	val oidcConfig: OidcConfig = get()
	val response = httpClient.get(oidcConfig.userInfoUrl) {
		bearerAuth(token)
	}
		.body<JsonObject>()

	return runCatching {
		response[oidcConfig.groupsClaim]?.jsonArray?.toList()?.map { it.jsonPrimitive.content }
	}
		.onFailure { exception ->
			KotlinLogging.logger {}.error(exception) { "Couldn't get user groups" }
		}
		.getOrNull()
		?: emptyList()
}
