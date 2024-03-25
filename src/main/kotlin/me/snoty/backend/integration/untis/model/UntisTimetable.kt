package me.snoty.backend.integration.untis.model

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.untis.model.timetable.Period

@Serializable
data class UntisTimetable(
	val displayableStartDate: String,
	val displayableEndDate: String,
	val periods: List<Period>
)
