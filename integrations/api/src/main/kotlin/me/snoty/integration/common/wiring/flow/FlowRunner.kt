package me.snoty.integration.common.wiring.flow

import me.snoty.integration.common.wiring.data.IntermediateData
import org.slf4j.Logger
import org.slf4j.event.Level

fun interface FlowRunner {
	suspend fun execute(
		jobId: String,
		logger: Logger,
		logLevel: Level,
		flow: WorkflowWithNodes,
		input: IntermediateData,
	)
}
