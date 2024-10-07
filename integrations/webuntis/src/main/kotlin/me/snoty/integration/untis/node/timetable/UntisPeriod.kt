package me.snoty.integration.untis.node.timetable

import me.snoty.integration.untis.model.*
import me.snoty.integration.untis.model.timetable.Period
import me.snoty.integration.untis.model.timetable.PeriodElement
import me.snoty.integration.untis.model.timetable.PeriodExam
import me.snoty.integration.untis.model.timetable.PeriodText

/**
 * Mapped from [Period]
 */
data class UntisPeriod(
	val id: Int,
	val raw: Period,
	val lessonId: Int,
	var startDateTime: UntisDateTime,
	var endDateTime: UntisDateTime,
	val foregroundColor: String,
	val backgroundColor: String,
	val innerForegroundColor: String,
	val innerBackgroundColor: String,
	val text: PeriodText,
	val elements: List<PeriodElement>,
	val `class`: LongMasterElement? = null,
	val teacher: TeacherMasterElement? = null,
	val subject: LongMasterElement? = null,
	val room: LongMasterElement? = null,
	/**
	 * Contains the codes of [Period.Companion]
	 */
	val `is`: List<String>,
	val exam: PeriodExam? = null,
	val blockHash: Int? = null
)

fun Period.toUntisPeriod(mappedMasterData: MappedMasterData) = UntisPeriod(
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

private fun Period.find(type: String) = elements.find { it.type == type }?.id
