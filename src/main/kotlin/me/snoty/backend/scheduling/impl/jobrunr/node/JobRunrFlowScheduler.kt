package me.snoty.backend.scheduling.impl.jobrunr.node

import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.scheduling.impl.jobrunr.JobRunrScheduler
import me.snoty.integration.common.utils.createFlowJob
import me.snoty.integration.common.wiring.flow.Workflow
import org.koin.core.annotation.Single

@Single
class JobRunrFlowScheduler(private val jobRunrScheduler: JobRunrScheduler) : FlowScheduler {
	override fun schedule(workflow: Workflow) {
		val jobRequest = JobRunrFlowJobRequest(workflow._id)
		val job = createFlowJob(workflow, jobRequest)
		jobRunrScheduler.scheduleJob(workflow._id.toString(), job)
	}
}
