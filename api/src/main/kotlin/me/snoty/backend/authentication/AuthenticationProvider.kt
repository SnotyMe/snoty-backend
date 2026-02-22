package me.snoty.backend.authentication

import io.ktor.server.application.*
import me.snoty.core.UserId

interface AuthenticationProvider {
	suspend fun getUserById(userId: UserId): User?

	suspend fun getRolesByToken(token: String): List<Role>
	suspend fun getRolesById(userId: UserId): List<Role>

	fun configureKtor(application: Application)
}
