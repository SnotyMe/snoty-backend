package me.snoty.backend.integration.moodle.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.moodle.model.raw.MoodleEvent
import java.time.Instant
import java.time.ZoneId

@Serializable
data class MoodleAssignment(
	val id: Long,
	val name: String,
	val due: LocalDateTime,
	val state: MoodleAssignmentState
)

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
