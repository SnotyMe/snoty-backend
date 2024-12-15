package me.snoty.integration.builtin.emitjson

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.Language
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
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
	displayName = "Emit JSON",
	settingsType = EmitJsonSettings::class,
	position = NodePosition.START,
	outputType = EmptySchema::class,
)
@Single
class EmitJsonNodeHandler : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>
	): NodeOutput = (node.settings as EmitJsonSettings)
		.data
		.map(Document::parse)
		.map(::BsonIntermediateData)
}
