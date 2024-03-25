package me.snoty.backend.integration.untis.model.timetable

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.untis.model.UnknownObject

@Serializable
data class PeriodText(
	val lesson: String,
	val substitution: String,
	val info: String,
	val staffInfo: UnknownObject? = null
)
