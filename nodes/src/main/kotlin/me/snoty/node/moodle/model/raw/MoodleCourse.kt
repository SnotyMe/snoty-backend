package me.snoty.node.moodle.model.raw

import kotlinx.serialization.Serializable

@Serializable
data class MoodleCourse(
	val id: Long,
	val fullname: String,
	val shortname: String,
)
