package me.snoty.node.limit

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.schema.FieldDefaultValue
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.node.NodeHandleContext
import me.snoty.backend.wiring.node.NodeHandler
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import org.koin.core.annotation.Single

@Serializable
data class LimitSettings(
	@FieldDescription("The maximum number of items to forward.")
	@FieldDefaultValue("10")
	val count: Int = 10,
) : NodeSettings

@RegisterNode(
	name = "limit",
	displayName = "Limit",
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
	settingsType = LimitSettings::class,
	stereotype = NodeStereotype.MIDDLE,
)
@Single
class LimitNode : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput {
		val settings = node.getConfig<LimitSettings>()
		return input.take(settings.count)
	}
}
