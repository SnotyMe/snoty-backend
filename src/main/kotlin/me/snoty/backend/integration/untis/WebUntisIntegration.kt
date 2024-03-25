package me.snoty.backend.integration.untis

import kotlinx.serialization.Serializable

@Serializable
data class WebUntisSettings(
	val baseUrl: String,
	val school: String,
	val username: String,
	val appSecret: String
)
