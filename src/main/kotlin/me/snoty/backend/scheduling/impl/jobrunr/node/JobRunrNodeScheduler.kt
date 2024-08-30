package me.snoty.backend.scheduling.impl.jobrunr.node

import me.snoty.backend.scheduling.NodeScheduler
import me.snoty.backend.scheduling.impl.jobrunr.JobRunrScheduler
import me.snoty.integration.common.utils.createFetcherJob
import me.snoty.integration.common.wiring.Node
import org.koin.core.annotation.Single

@Single
class JobRunrNodeScheduler(private val jobRunrScheduler: JobRunrScheduler) : NodeScheduler {
	override fun schedule(node: Node) {
		val jobRequest = JobRunrNodeJobRequest(node._id)
		val job = createFetcherJob(node.descriptor, node, jobRequest)
		jobRunrScheduler.scheduleJob(node._id.toString(), job)
	}
}
