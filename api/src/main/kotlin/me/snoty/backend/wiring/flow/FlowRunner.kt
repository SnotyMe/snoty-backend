package me.snoty.backend.wiring.flow

import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.core.flow.WorkflowWithNodes
import me.snoty.core.node.FlowNode
import org.slf4j.Logger
import org.slf4j.event.Level

fun interface FlowRunner {
	suspend fun execute(
		jobId: String,
		triggeredBy: FlowTriggerReason,
		logger: Logger,
		logLevel: Level,
		flow: WorkflowWithNodes,
		startNode: FlowNode,
		input: NodeInput,
	)
}
