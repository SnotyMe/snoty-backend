package me.snoty.integration.common.wiring.flow

import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.DEFAULT_SCHEDULE
import me.snoty.backend.scheduling.JobSchedule
import me.snoty.integration.common.wiring.FlowNode
import org.jobrunr.scheduling.cron.CronExpression
import org.jobrunr.scheduling.cron.InvalidCronExpressionException
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

interface Workflow {
	val _id: String
	val name: String
	val userId: Uuid
	val settings: WorkflowSettings
}

@Serializable
data class WorkflowSettings(
	val schedule: JobSchedule = DEFAULT_SCHEDULE,
) {
	init {
		when (schedule) {
			is JobSchedule.Recurring -> validateRecurring(schedule.interval)
			is JobSchedule.Cron -> validateCron(schedule.expression)
			JobSchedule.Never -> Unit
		}
	}

	private fun validateRecurring(interval: Duration) {
		require(interval >= 5.minutes) {
			"Schedule must be at least 5 minutes"
		}
		require(interval <= 24.hours) {
			"Schedule must be at most 24 hours"
		}
	}

	private fun validateCron(expression: String) {
		require(expression.isNotBlank()) {
			"Cron expression must not be blank"
		}
		val fields = expression.trim().lowercase(Locale.ROOT).split("\\s+".toRegex())
		require(fields.size == 5) {
			"Cron expression must have 5 parts. 6 parts (seconds resolution) are not supported to prevent spam."
		}
		try {
			CronExpression(expression)
		} catch (cronException: InvalidCronExpressionException) {
			throw IllegalArgumentException("Invalid cron expression: ${cronException.message}", cronException)
		}
	}
}

/**
 * High-Level representation of a workflow, without any nodes.
 */
@Serializable
data class StandaloneWorkflow(
	override val _id: String,
	override val name: String,
	override val userId: Uuid,
	override val settings: WorkflowSettings,
) : Workflow

/**
 * High-Level representation of a workflow, with the involved nodes.
 */
 @Serializable
data class WorkflowWithNodes(
	override val _id: String,
	override val name: String,
	override val userId: Uuid,
	override val settings: WorkflowSettings,
	/**
	 * A list of all nodes.
	 * These nodes are normalized. Can include nodes not connected to anything.
	 */
	val nodes: List<FlowNode>,
) : Workflow
