package me.snoty.integration.untis.model.timetable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeriodExam(
	val id: Int,
	@SerialName("examtype")
	val examType: String?,
	val name: String?,
	val text: String?
)
