package me.snoty.integration.builtin.limit

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.core.annotation.Single

@Serializable
data class LimitSettings(
	override val name: String = "Limit",
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
	position = NodePosition.MIDDLE,
)
@Single
class LimitNode : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: Collection<IntermediateData>): NodeOutput {
		val settings = node.getConfig<LimitSettings>()
		return input.take(settings.count)
	}
}
