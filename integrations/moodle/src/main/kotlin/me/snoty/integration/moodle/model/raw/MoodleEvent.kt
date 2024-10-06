package me.snoty.integration.moodle.model.raw

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoodleEvent(
	val id: Long,
	val instance: Long,
	@SerialName("activityname")
	val name: String,
	val description: String,
	@SerialName("timestart")
	val timeStart: Long,
	val overdue: Boolean,
	val action: MoodleAction? = null
)
