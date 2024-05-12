package me.snoty.integration.moodle.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.snoty.integration.common.diff.UpdatableEntity
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.common.diff.getString
import me.snoty.integration.moodle.model.raw.MoodleEvent
import java.time.Instant
import java.time.ZoneId

@Serializable
data class MoodleAssignment(
	override val id: Long,
	val name: String,
	val due: LocalDateTime,
	val state: MoodleAssignmentState
) : UpdatableEntity<Long>() {
	override val type: String = TYPE

	@Contextual
	override val fields: Fields = buildJsonObject {
		put("name", name)
		put("due", due.toString())
		put("state", state.name)
	}

	companion object {
		const val TYPE = "assignment"

		fun fromFields(id: Long, fields: Fields): MoodleAssignment {
			return MoodleAssignment(
				id = id,
				name = fields.getString("name"),
				due = LocalDateTime.parse(fields.getString("due")),
				state = MoodleAssignmentState.valueOf(fields.getString("state"))
			)
		}
	}
}

enum class MoodleAssignmentState {
	/**
	 * you cannot submit anything,
	 * even though the assignment is in the future
	 */
	CLOSED,
	DUE,
	DONE,
	OVERDUE,
}

fun MoodleEvent.toMoodleAssignment(): MoodleAssignment {
	return MoodleAssignment(
		id = id,
		name = name,
		due = Instant.ofEpochSecond(timeStart).atZone(ZoneId.systemDefault()).toLocalDateTime().toKotlinLocalDateTime(),
		state = when {
			overdue -> MoodleAssignmentState.OVERDUE
			action?.actionable == true -> MoodleAssignmentState.DUE
			action?.actionable == false -> MoodleAssignmentState.CLOSED
			else -> MoodleAssignmentState.DONE
		}
	)
}
