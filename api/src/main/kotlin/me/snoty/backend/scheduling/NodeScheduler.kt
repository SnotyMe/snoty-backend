package me.snoty.backend.scheduling

import me.snoty.backend.wiring.data.NodeInput
import me.snoty.core.node.NodeWithSettings
import org.slf4j.event.Level

interface NodeScheduler {
	fun schedule(node: NodeWithSettings, schedule: JobSchedule)

	fun trigger(node: NodeWithSettings, logLevel: Level, input: NodeInput): JobTriggerResult?

	fun unschedule(node: NodeWithSettings)
}
