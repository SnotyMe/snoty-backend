package me.snoty.backend.scheduling

import me.snoty.integration.common.wiring.flow.Workflow

interface FlowScheduler {
	fun schedule(workflow: Workflow)
}
