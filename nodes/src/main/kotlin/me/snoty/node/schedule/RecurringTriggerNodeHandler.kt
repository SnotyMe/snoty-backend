package me.snoty.node.schedule

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.snoty.backend.scheduling.JobSchedule
import me.snoty.backend.scheduling.Schedulable
import me.snoty.backend.schema.DurationLimits
import me.snoty.backend.schema.FieldDefaultValue
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
	@FieldDefaultValue("PT15M")
	@DurationLimits(min = MIN, max = MAX)
	val interval: Duration,
) : NodeSettings, Schedulable {

	companion object {
		const val MIN = "PT5M"
		val MIN_DURATION = Duration.parse(MIN)
		const val MAX = "PT24H"
		val MAX_DURATION = Duration.parse(MAX)
	}

	init {
		require(interval >= MIN_DURATION) {
			"Schedule must be at least $MIN_DURATION"
		}
		require(interval <= MAX_DURATION) {
			"Schedule must be at most $MAX_DURATION"
		}
	}

	@Transient
	override val schedule = JobSchedule.Recurring(interval)
}

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
