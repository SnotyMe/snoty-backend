package me.snoty.backend

import kotlinx.serialization.Serializable
import me.snoty.backend.utils.UUIDSerializer
import java.util.*

@Serializable
data class User(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val name: String,
	val email: String,
)
