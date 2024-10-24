package me.snoty.integration.moodle.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.snoty.integration.moodle.model.raw.MoodleCourse
import me.snoty.integration.moodle.model.raw.MoodleEvent

@Serializable
data class MoodleAssignment(
	val id: Long,
	val instance: Long,
	val name: String,
	val description: String,
	val due: Instant,
	val state: MoodleAssignmentState,
	val course: MoodleCourse,
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

fun MoodleEvent.toMoodleAssignment() = MoodleAssignment(
	id = id,
	instance = instance,
	name = name,
	description = description,
	due = Instant.fromEpochSeconds(timeStart),
	state = when {
		overdue -> MoodleAssignmentState.OVERDUE
		action?.actionable == true -> MoodleAssignmentState.DUE
		action?.actionable == false -> MoodleAssignmentState.CLOSED
		else -> MoodleAssignmentState.DONE
	},
	course = course,
)
