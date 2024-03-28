package me.snoty.backend.integration.moodle.param

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
