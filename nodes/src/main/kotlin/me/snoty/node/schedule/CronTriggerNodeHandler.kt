package me.snoty.node.schedule

import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.JobSchedule
import me.snoty.backend.scheduling.Schedulable
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.impl.EmptyIntermediateData
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import org.koin.core.annotation.Single

@Serializable
data class CronTriggerNodeSettings(
	val expression: String,
) : NodeSettings, Schedulable {
	override val schedule = JobSchedule.Cron(expression)
}

@RegisterNode(
	name = CronTriggerNodeHandler.TYPE,
	displayName = "CRON Schedule",
	icon = Icon(name = "lucide-calendar-clock"),
	stereotype = NodeStereotype.START,
	settingsType = CronTriggerNodeSettings::class,
	outputType = Unit::class,
)
@Single
class CronTriggerNodeHandler : NodeHandler {
	companion object {
		const val TYPE = "cron_schedule_trigger"
	}

	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput =
		listOf(EmptyIntermediateData)
}
