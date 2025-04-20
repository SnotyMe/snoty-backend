package me.snoty.backend

import kotlinx.serialization.Serializable

@Serializable
data class User(
	val id: String,
	val name: String,
	val email: String,
)
