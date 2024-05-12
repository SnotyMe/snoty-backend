package me.snoty.integration.untis.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
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
	override val type: String = "exam"

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
