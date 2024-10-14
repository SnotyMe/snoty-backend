package me.snoty.backend.scheduling

import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.Workflow

interface FlowScheduler {
	/**
	 * Schedule recurring jobs
	 */
	fun schedule(workflow: Workflow, jobRequest: FlowJobRequest = FlowJobRequest())

	/**
	 * Trigger a one-off workflow
	 */
	fun trigger(workflow: Workflow, jobRequest: FlowJobRequest)

	/**
	 * @param flowService manually injected FlowService to avoid circular dependencies
	 */
	suspend fun scheduleMissing(flowService: FlowService)

	fun jobId(workflow: Workflow): String = workflow._id.toString()
}
