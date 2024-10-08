package me.snoty.integration.untis.node.exam

import me.snoty.integration.untis.model.*

data class MappedUntisExam(
	val id: Int,
	val raw: UntisExam,
	val examType: String?,
	val startDateTime: UntisDateTime,
	val endDateTime: UntisDateTime,
	val departmentId: Int,
	val subject: LongMasterElement?,
	val classes: List<LongMasterElement>,
	val rooms: List<LongMasterElement>,
	val teachers: List<TeacherMasterElement>,
	val name: String,
	val text: String,
)

fun UntisExam.map(mappedMasterData: MappedMasterData) = MappedUntisExam(
	id = id,
	raw = this,
	examType = examType,
	startDateTime = startDateTime,
	endDateTime = endDateTime,
	departmentId = departmentId,
	subject = mappedMasterData.subjects[subjectId],
	classes = klasseIds.map { mappedMasterData.classes[it] ?: error("Class not found for id $it") },
	rooms = roomIds.map { mappedMasterData.rooms[it] ?: error("Room not found for id $it") },
	teachers = teacherIds.map { mappedMasterData.teachers[it] ?: error("Teacher not found for id $it") },
	name = name,
	text = text,
)
