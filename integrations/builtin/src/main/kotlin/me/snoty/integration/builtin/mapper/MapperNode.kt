package me.snoty.integration.builtin.mapper

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.mapInputWithSettings
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import me.snoty.integration.common.wiring.structOutput
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class MapperSettings(
	override val name: String = "Mapper",
	val engine: MapperEngine,
	@FieldDescription("The fields to map - every key will be part of the output object")
	val fields: Map<String, String>,
	@FieldDescription("If true, the ID of the input object will be preserved in the output object")
	val preserveId: Boolean = true
) : NodeSettings

@RegisterNode(
	displayName = "Mapper",
	type = "mapper",
	subsystem = Subsystem.PROCESSOR,
	position = NodePosition.MIDDLE,
	settingsType = MapperSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class
)
@Single
class MapperNodeHandler : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>,
	) = mapInputWithSettings<Document, MapperSettings>(input, node) { data, settings ->
		val mappedData = settings.engine.template(logger, settings, data)

		structOutput(mappedData)
	}
}
