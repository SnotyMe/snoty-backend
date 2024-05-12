package me.snoty.integration.untis.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import me.snoty.integration.common.diff.*

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
				startDateTime = UntisDateTime.fromString(fields.getString("startDateTime")),
				endDateTime = UntisDateTime.fromString(fields.getString("endDateTime")),
				departmentId = fields.getInt("departmentId"),
				subjectId = fields.getInt("subjectId"),
				klasseIds = fields.getJsonArray("klasseIds").map { it.jsonPrimitive.int },
				roomIds = fields.getJsonArray("roomIds").map { it.jsonPrimitive.int },
				teacherIds = fields.getJsonArray("teacherIds").map { it.jsonPrimitive.int },
				name = fields.getString("name"),
				text = fields.getString("text")
			)
		}
	}

	@Contextual
	override val fields: Fields = buildJsonObject {
		put("examType", examType)
		put("startDateTime", startDateTime.toString())
		put("endDateTime", endDateTime.toString())
		put("departmentId", departmentId)
		put("subjectId", subjectId)
		putJsonArray("roomIds") {
			roomIds.forEach { add(it) }
		}
		putJsonArray("klasseIds") {
			klasseIds.forEach { add(it) }
		}
		putJsonArray("teacherIds") {
			teacherIds.forEach { add(it) }
		}
		put("name", name)
		put("text", text)
	}
}
