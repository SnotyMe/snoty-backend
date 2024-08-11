package me.snoty.integration.builtin.mapper

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.FieldName
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.slf4j.Logger
import kotlin.reflect.KClass

@Serializable
data class MapperSettings(
	override val name: String = "Mapper",
	val engine: MapperEngine,
	@FieldDescription("The fields to map - every key will be part of the output object")
	val fields: Map<String, String>
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
class MapperNodeHandler(override val nodeHandlerContext: NodeHandlerContext) : NodeHandler {
	override val settingsClass: KClass<out NodeSettings> = MapperSettings::class

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val settings: MapperSettings = node.getConfig()
		val data: Document = input.get()
		val mappedData = settings.engine.templater(settings, data)

		structOutput {
			mappedData
		}
	}
}
