package me.snoty.backend.integration.moodle

import kotlinx.serialization.Serializable

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	val appSecret: String
)
