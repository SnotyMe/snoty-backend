package me.snoty.backend.integration.moodle.param

import me.snoty.integration.moodle.param.MoodleParam

data class UserByFieldMoodleParam(
	val field: String,
	val value: String
) : MoodleParam {
	override fun toMap(): Map<String, String> {
		return mapOf(
			"field" to field,
			"values[0]" to value
		)
	}
}
