package me.snoty.integration.untis.model

import kotlinx.serialization.Serializable
import me.snoty.integration.untis.model.timetable.UntisPeriod

@Serializable
data class UntisTimetable(
	val displayableStartDate: String,
	val displayableEndDate: String,
	val periods: List<UntisPeriod>
)
