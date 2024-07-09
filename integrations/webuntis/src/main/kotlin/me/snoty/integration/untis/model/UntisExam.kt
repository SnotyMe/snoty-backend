package me.snoty.integration.untis.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.common.diff.UpdatableEntity

@Serializable
data class UntisExam(
	override val id: Int,
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
) : UpdatableEntity<Int>() {
	override val type: String = TYPE

	companion object {
		const val TYPE = "exam"

		fun fromFields(id: Int, fields: Fields): UntisExam {
			return UntisExam(
				id = id,
				examType = fields.getString("examType"),
				startDateTime = UntisDateTime.fromDate(fields.getDate("startDateTime")),
				endDateTime = UntisDateTime.fromDate(fields.getDate("endDateTime")),
				departmentId = fields.getInteger("departmentId"),
				subjectId = fields.getInteger("subjectId"),
				klasseIds = fields.getList("klasseIds", Int::class.java),
				roomIds = fields.getList("roomIds", Int::class.java),
				teacherIds = fields.getList("teacherIds", Int::class.java),
				name = fields.getString("name"),
				text = fields.getString("text")
			)
		}
	}

	override fun prepareFieldsForDiff(fields: Fields) {
		fields["startDateTime"] = UntisDateTime.fromDate(fields.getDate("startDateTime"))
		fields["endDateTime"] = UntisDateTime.fromDate(fields.getDate("endDateTime"))
	}

	@Transient
	override val fields: Fields = buildDocument {
		put("examType", examType)
		put("startDateTime", startDateTime)
		put("endDateTime", endDateTime)
		put("departmentId", departmentId)
		put("subjectId", subjectId)
		put("roomIds", roomIds)
		put("klasseIds", klasseIds)
		put("teacherIds", teacherIds)
		put("name", name)
		put("text", text)
	}
}
