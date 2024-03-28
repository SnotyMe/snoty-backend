package me.snoty.backend.integration.untis.model

import kotlinx.serialization.Serializable

@Serializable
data class UntisExam(
	val id: Int,
	val examType: String?,
	val startDateTime: UntisDateTime,
	val endDateTime: UntisDateTime,
	val departmentId: Int,
	val subjectId: Int,
	val klasseIds: List<Int>,
	val roomIds: List<Int>,
	val teacherIds: List<Int>,
	val name: String,
	val text: String
)
