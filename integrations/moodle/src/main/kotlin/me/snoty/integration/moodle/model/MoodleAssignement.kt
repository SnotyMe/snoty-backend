package me.snoty.integration.moodle.model

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.common.diff.UpdatableEntity
import me.snoty.integration.moodle.model.raw.MoodleEvent

@Serializable
data class MoodleAssignment(
	override val id: Long,
	val name: String,
	val due: Instant,
	val state: MoodleAssignmentState
) : UpdatableEntity<Long>() {
	override val type: String = TYPE

	@Contextual
	override val fields: Fields = buildDocument {
		put("name", name)
		put("due", due)
		put("state", state.name)
	}

	override fun prepareFieldsForDiff(fields: Fields) {
		fields["due"] = fields.getDate("due").toInstant().toKotlinInstant()
	}

	companion object {
		const val TYPE = "assignment"

		fun fromFields(id: Long, fields: Fields): MoodleAssignment {
			return MoodleAssignment(
				id = id,
				name = fields.getString("name"),
				due = fields.getDate("due").toInstant().toKotlinInstant(),
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
		due = Instant.fromEpochSeconds(timeStart),
		state = when {
			overdue -> MoodleAssignmentState.OVERDUE
			action?.actionable == true -> MoodleAssignmentState.DUE
			action?.actionable == false -> MoodleAssignmentState.CLOSED
			else -> MoodleAssignmentState.DONE
		}
	)
}
