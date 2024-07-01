package me.snoty.backend.scheduling.node

import me.snoty.backend.scheduling.JobRunrScheduler
import me.snoty.integration.common.utils.createFetcherJob
import me.snoty.integration.common.wiring.IFlowNode

interface NodeScheduler {
	fun schedule(node: IFlowNode)
}

class NodeSchedulerImpl(private val jobRunrScheduler: JobRunrScheduler) : NodeScheduler {
	override fun schedule(node: IFlowNode) {
		val jobRequest = NodeJobRequest(node._id)
		val job = createFetcherJob(node.descriptor, node, jobRequest)
		jobRunrScheduler.scheduleJob(node._id.toString(), job)
	}
}
