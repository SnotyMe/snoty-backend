package me.snoty.backend.authentication

import kotlinx.serialization.Serializable

@Serializable
data class Role(
	val name: String,
) {
	companion object {
		val ADMIN = Role("admin")
		val MANAGE_CREDENTIALS = Role("manage-credentials")
	}
}
