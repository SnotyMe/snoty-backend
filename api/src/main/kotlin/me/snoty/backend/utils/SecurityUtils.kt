package me.snoty.backend.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import me.snoty.backend.User
import java.util.*

fun ApplicationCall.getUser(): User =
	getUserOrNull() ?: throw UnauthorizedException("User not authenticated")

fun ApplicationCall.getUserOrNull(): User? {
	val principal = authentication.principal<JWTPrincipal>() ?: return null
	val claims = principal.payload.claims
	return User(
		id = claims["sub"]?.`as`(UUID::class.java) ?: NULL_UUID,
		name = claims["preferred_username"]?.asString() ?: "unknown",
		email = claims["email"]?.asString() ?: "unknown"
	)
}
