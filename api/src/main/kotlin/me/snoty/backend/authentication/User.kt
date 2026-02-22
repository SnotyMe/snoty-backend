package me.snoty.backend.authentication

import kotlinx.serialization.Serializable
import me.snoty.core.UserId

@Serializable
data class User(
	val id: UserId,
	val name: String,
	val email: String,
)
