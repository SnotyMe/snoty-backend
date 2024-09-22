package me.snoty.backend.scheduling

import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.Workflow

interface FlowScheduler {
	fun schedule(workflow: Workflow)

	/**
	 * @param flowService manually injected FlowService to avoid circular dependencies
	 */
	suspend fun scheduleMissing(flowService: FlowService)

	fun jobId(workflow: Workflow): String = workflow._id.toString()
}
