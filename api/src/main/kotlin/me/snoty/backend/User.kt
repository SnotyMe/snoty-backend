package me.snoty.backend

import java.util.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.UUIDSerializer

/*
@Serializable
*/
@Serializable
data class User(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val name: String,
	val email: String,
)
