package me.snoty.integration.untis.node.timetable

import me.snoty.integration.untis.model.*
import me.snoty.integration.untis.model.timetable.UntisPeriod
import me.snoty.integration.untis.model.timetable.UntisPeriodElement
import me.snoty.integration.untis.model.timetable.UntisPeriodExam
import me.snoty.integration.untis.model.timetable.UntisPeriodText

/**
 * Mapped from [UntisPeriod]
 */
data class MappedUntisPeriod(
	val id: Int,
	val raw: UntisPeriod,
	val lessonId: Int,
	var startDateTime: UntisDateTime,
	var endDateTime: UntisDateTime,
	val foregroundColor: String,
	val backgroundColor: String,
	val innerForegroundColor: String,
	val innerBackgroundColor: String,
	val text: UntisPeriodText,
	val elements: List<UntisPeriodElement>,
	val `class`: LongMasterElement? = null,
	val teacher: TeacherMasterElement? = null,
	val subject: LongMasterElement? = null,
	val room: LongMasterElement? = null,
	/**
	 * Contains the codes of [UntisPeriod.Companion]
	 */
	val `is`: List<String>,
	val exam: UntisPeriodExam? = null,
	val blockHash: Int? = null
)

fun UntisPeriod.toUntisPeriod(mappedMasterData: MappedMasterData) = MappedUntisPeriod(
	id = id,
	raw = this,
	lessonId = lessonId,
	startDateTime = startDateTime,
	endDateTime = endDateTime,
	foregroundColor = foreColor,
	backgroundColor = backColor,
	innerForegroundColor = innerForeColor,
	innerBackgroundColor = innerBackColor,
	text = text,
	elements = elements,
	`class` = mappedMasterData.classes[find("CLASS")],
	teacher = mappedMasterData.teachers[find("TEACHER")],
	subject = mappedMasterData.subjects[find("SUBJECT")],
	room = mappedMasterData.rooms[find("ROOM")],
	`is` = `is`,
	exam = exam,
	blockHash = blockHash,
)

private fun UntisPeriod.find(type: String) = elements.find { it.type == type }?.id
