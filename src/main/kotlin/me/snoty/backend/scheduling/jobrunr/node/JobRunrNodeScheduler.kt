package me.snoty.backend.scheduling.jobrunr.node

import me.snoty.backend.scheduling.*
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.impl.EmptyIntermediateData
import me.snoty.core.node.NodeWithSettings
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class JobRunrNodeScheduler(
	private val jobRunrScheduler: Scheduler,
) : NodeScheduler {
	override fun schedule(node: NodeWithSettings, schedule: JobSchedule) {
		val jobRequest = JobRunrNodeJobRequest(
			nodeId = node.id,
			flowId = node.flowId,
			triggeredBy = FlowTriggerReason.Scheduled,
			input = listOf(EmptyIntermediateData),
		)

		val job = SnotyJob(
			recurringJobId = jobId(node),
			name = jobName(node),
			retries = 0,
			request = jobRequest,
			schedule = schedule,
		)

		jobRunrScheduler.scheduleRecurringJob(job)
	}

	override fun trigger(node: NodeWithSettings, logLevel: Level, input: NodeInput): JobTriggerResult? {
		val jobRequest = JobRunrNodeJobRequest(
			nodeId = node.id,
			flowId = node.flowId,
			triggeredBy = FlowTriggerReason.Manual,
			logLevel = logLevel,
			input = input,
		)

		val job = SnotyJob(
			recurringJobId = jobId(node),
			name = jobName(node),
			retries = 0,
			request = jobRequest,
		)

		return jobRunrScheduler.triggerRecurringJobOrSchedule(job)
	}

	override fun unschedule(node: NodeWithSettings) {
		jobRunrScheduler.deleteRecurringJob(jobId(node))
	}

	private fun jobId(node: NodeWithSettings) = "${node.flowId.value}_${node.id.value}"
	private fun jobName(node: NodeWithSettings) =
		"Node type=${node.type.value} id=${node.id.value} user=${node.userId.value} name=${node.name}"
}
