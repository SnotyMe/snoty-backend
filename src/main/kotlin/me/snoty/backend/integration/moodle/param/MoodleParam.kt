package me.snoty.backend.integration.moodle.param

interface MoodleParam {
	fun toMap(): Map<String, String>
}
