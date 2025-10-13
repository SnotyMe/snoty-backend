package me.snoty.integration.builtin.json.emit

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.Language
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.iterableStructOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class EmitJsonSettings(
	override val name: String = "Emit JSON",
	val data: List<@Language("json") String>,
) : NodeSettings

@RegisterNode(
	name = "emitjson",
	namespace = "me.snoty.integration.builtin.emitjson",
	displayName = "Emit JSON",
	settingsType = EmitJsonSettings::class,
	position = NodePosition.START,
	outputType = EmptySchema::class,
)
@Single
class EmitJsonNodeHandler : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: Node,
		input: Collection<IntermediateData>
	): NodeOutput = iterableStructOutput(
		node.getConfig<EmitJsonSettings>()
		.data
		.map(Document::parse)
	)
}
