package me.snoty.backend.authentication

import io.ktor.server.application.*

interface AuthenticationProvider {
	suspend fun getUserById(userId: String): User?

	suspend fun getRolesByToken(token: String): List<Role>
	suspend fun getRolesById(userId: String): List<Role>

	fun configureKtor(application: Application)
}
