package me.snoty.node.json.emit

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.schema.Language
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.iterableStructOutput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class EmitJsonSettings(
	val data: List<@Language("json") String>,
) : NodeSettings

@RegisterNode(
	name = "emitjson",
	displayName = "Emit JSON",
	icon = Icon(name = "lucide-braces"),
	settingsType = EmitJsonSettings::class,
	stereotype = NodeStereotype.START,
	outputType = EmptySchema::class,
)
@Single
class EmitJsonNodeHandler : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput =
		iterableStructOutput(
			node.getConfig<EmitJsonSettings>()
			.data
			.map(Document::parse)
		)
}
