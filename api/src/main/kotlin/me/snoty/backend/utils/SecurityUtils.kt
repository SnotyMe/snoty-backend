package me.snoty.backend.utils

import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.authentication.User
import me.snoty.core.UserId
import org.koin.ktor.ext.get

fun ApplicationRequest.parseAuthHeader() =
	call.request.parseAuthorizationHeader()
		// optionally load from cookies
		?: parseAuthorizationHeader("Bearer ${call.request.cookies["access_token"]}")

fun ApplicationCall.getUser(): User =
	getUserOrNull() ?: throw UnauthorizedException("User not authenticated")

fun ApplicationCall.getUserOrNull(): User? {
	val principal = authentication.principal<JWTPrincipal>() ?: return null
	val claims = principal.payload.claims

	val id = claims["sub"]?.asString() ?: return null
	return User(
		id = UserId(id),
		name = claims["preferred_username"]?.asString() ?: "unknown",
		email = claims["email"]?.asString() ?: "unknown",
	)
}

suspend fun ApplicationCall.getUserRoles(): List<Role> {
	val token = request.parseAuthHeader() as? HttpAuthHeader.Single
		?: throw UnauthorizedException("Invalid token")

	val authenticationProvider: AuthenticationProvider = get()
	return authenticationProvider.getRolesByToken(token.blob)
}

suspend fun ApplicationCall.requireAnyRole(vararg roles: Role) {
	val userRoles = getUserRoles()

	if (userRoles.hasAnyRole(*roles)) return

	val rolesFormatted = roles.joinToString(", ") { it.name }
	throw ForbiddenException("Missing roles '$rolesFormatted'")
}

fun List<Role>.hasAnyRole(vararg needsAnyOf: Role): Boolean =
	needsAnyOf.any { this.contains(it) }
