package me.snoty.integration.common.wiring.flow

import me.snoty.integration.common.wiring.data.IntermediateData
import org.slf4j.Logger

fun interface FlowRunner {
	suspend fun execute(
		jobId: String,
		logger: Logger,
		flow: WorkflowWithNodes,
		input: IntermediateData,
	)
}
