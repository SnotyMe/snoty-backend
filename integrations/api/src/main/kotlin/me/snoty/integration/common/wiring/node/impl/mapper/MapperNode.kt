package me.snoty.integration.common.wiring.node.impl.mapper

import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.*
import org.bson.Document
import org.slf4j.Logger
import kotlin.reflect.KClass

@Serializable
data class MapperSettings(
	override val name: String = "Mapper",
	val engine: String,
	val fields: Map<String, String>
) : NodeSettings

class MapperNodeHandler(override val nodeHandlerContext: NodeHandlerContext) : NodeHandler {
	override val settingsClass: KClass<out NodeSettings> = MapperSettings::class
	override val position: NodePosition = NodePosition.MIDDLE

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val settings: MapperSettings = node.getConfig()
		val data: Document = input.get()
		val engine = MapperEngines.get(settings)
			?: throw IllegalStateException("Engine not found!")

		val mappedData = engine(settings, data)

		structOutput {
			mappedData
		}
	}
}

class MapperNodeHandlerContributor : NodeHandlerContributor {
	override fun contributeHandlers(registry: NodeRegistry, nodeContextBuilder: NodeContextBuilder) {
		registry.registerHandler(Subsystem.PROCESSOR, "mapper", nodeContextBuilder) {
			MapperNodeHandler(it)
		}
	}
}
