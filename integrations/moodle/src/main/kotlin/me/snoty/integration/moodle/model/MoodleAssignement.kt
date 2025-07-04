package me.snoty.integration.moodle.model

import kotlinx.serialization.Serializable
import me.snoty.integration.moodle.model.raw.MoodleCourse
import me.snoty.integration.moodle.model.raw.MoodleEvent
import kotlin.time.Instant

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
	/**
	 * the assignment is in the past and you can no longer submit anything
	 * used to fix the Moodle API returning assignments in the past in the "upcoming events" endpoint
	 */
	PAST,
}

fun MoodleEvent.toMoodleAssignment() = MoodleAssignment(
	id = id,
	instance = instance,
	name = name,
	description = description,
	due = Instant.fromEpochSeconds(timeStart),
	state = when {
		// overdue but the user can still submit something
		overdue && action != null -> MoodleAssignmentState.OVERDUE
		// overdue but the user cannot submit - fixes #46
		overdue -> MoodleAssignmentState.PAST
		action?.actionable == true -> MoodleAssignmentState.DUE
		action?.actionable == false -> MoodleAssignmentState.CLOSED
		else -> MoodleAssignmentState.DONE
	},
	course = course,
)
