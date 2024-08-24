package me.snoty.backend.scheduling.node

import me.snoty.backend.scheduling.JobRunrScheduler
import me.snoty.integration.common.utils.createFetcherJob
import me.snoty.integration.common.wiring.Node
import org.koin.core.annotation.Single

interface NodeScheduler {
	fun schedule(node: Node)
}

@Single
class JobRunrNodeScheduler(private val jobRunrScheduler: JobRunrScheduler) : NodeScheduler {
	override fun schedule(node: Node) {
		val jobRequest = NodeJobRequest(node._id)
		val job = createFetcherJob(node.descriptor, node, jobRequest)
		jobRunrScheduler.scheduleJob(node._id.toString(), job)
	}
}
