package me.snoty.integration.untis.model.timetable

import kotlinx.serialization.Serializable
import me.snoty.integration.untis.model.UntisDateTime

@Serializable
data class Period(
	val id: Int,
	val lessonId: Int,
	var startDateTime: UntisDateTime,
	var endDateTime: UntisDateTime,
	val foreColor: String,
	val backColor: String,
	val innerForeColor: String,
	val innerBackColor: String,
	val text: PeriodText,
	val elements: List<PeriodElement>,
	val can: List<String>,
	// contains the codes from the companion object
	val `is`: List<String>,
	val exam: PeriodExam? = null,
	val blockHash: Int? = null
) {
	companion object {
		const val CODE_REGULAR = "REGULAR"
		const val CODE_CANCELLED = "CANCELLED"
		const val CODE_IRREGULAR = "IRREGULAR"
		const val CODE_EXAM = "EXAM"
	}
}
