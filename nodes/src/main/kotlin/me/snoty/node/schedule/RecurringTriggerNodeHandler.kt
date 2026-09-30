package me.snoty.node.schedule

import kotlinx.serialization.Serializable
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.impl.EmptyIntermediateData
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import org.koin.core.annotation.Single
import kotlin.time.Duration

@Serializable
data class RecurringTriggerNodeSettings(
	val interval: Duration,
) : NodeSettings

@RegisterNode(
	name = RecurringTriggerNodeHandler.TYPE,
	displayName = "Recurring Schedule",
	icon = Icon(name = "lucide-clock"),
	stereotype = NodeStereotype.START,
	settingsType = RecurringTriggerNodeSettings::class,
	outputType = Unit::class,
)
@Single
class RecurringTriggerNodeHandler : NodeHandler {
	companion object {
		const val TYPE = "recurring_schedule_trigger"
	}

	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput =
		listOf(EmptyIntermediateData)
}
