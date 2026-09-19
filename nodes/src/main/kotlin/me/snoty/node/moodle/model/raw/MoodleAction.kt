package me.snoty.node.moodle.model.raw

import kotlinx.serialization.Serializable

@Serializable
data class MoodleAction(
	val name: String,
	val url: String,
	val actionable: Boolean
)
