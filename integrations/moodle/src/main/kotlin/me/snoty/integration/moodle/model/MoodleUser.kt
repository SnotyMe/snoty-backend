package me.snoty.integration.moodle.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoodleUser(
	val id: Long,
	val username: String,
	@SerialName("fullname")
	val fullName: String,
	val email: String
)
