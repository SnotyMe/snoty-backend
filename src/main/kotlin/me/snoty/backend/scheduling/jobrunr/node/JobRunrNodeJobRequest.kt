package me.snoty.backend.scheduling.jobrunr.node

import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.utils.Slf4jLevelSerializer
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.core.flow.FlowId
import me.snoty.core.node.NodeId
import org.slf4j.event.Level

@Serializable
data class JobRunrNodeJobRequest(
	val flowId: FlowId,
	val nodeId: NodeId,
	val triggeredBy: FlowTriggerReason = FlowTriggerReason.Unknown,
	@Serializable(with = Slf4jLevelSerializer::class)
	val logLevel: Level = Level.INFO,
	val input: NodeInput,
) : JobRequest {
	override fun getJobRequestHandler() = JobRunrNodeJobHandler::class.java
}
