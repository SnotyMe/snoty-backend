package me.snoty.integration.common.utils

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.SnotyJob
import me.snoty.integration.common.wiring.flow.Workflow

fun createFlowJob(workflow: Workflow, request: JobRequest): SnotyJob {
	val user = workflow.userId
	return SnotyJob(
		name = "[${workflow._id}] user=$user flow='${workflow.name}'",
		request
	)
}
