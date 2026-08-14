package me.snoty.backend.scheduling

import me.snoty.core.flow.Workflow
import me.snoty.integration.common.wiring.flow.FlowService

interface FlowScheduler {
	/**
	 * Schedule recurring jobs
	 */
	fun schedule(workflow: Workflow, jobRequest: FlowJobRequest = FlowJobRequest(triggeredBy = FlowTriggerReason.Scheduled))

	/**
	 * Trigger a one-off workflow
	 */
	fun trigger(workflow: Workflow, jobRequest: FlowJobRequest)

	/**
	 * @param flowService manually injected FlowService to avoid circular dependencies
	 */
	suspend fun scheduleMissing(flowService: FlowService)

	suspend fun reschedule(workflow: Workflow)

	fun deleteAll(workflow: Workflow)

	fun jobId(workflow: Workflow): String = workflow.id.value
	fun jobName(workflow: Workflow): String = workflow.run { "[${id.value}] user=${userId.value} flow=\"$name\"" }
}

val DEFAULT_FLOW_JOB_REQUEST = FlowJobRequest(triggeredBy = FlowTriggerReason.Scheduled)
