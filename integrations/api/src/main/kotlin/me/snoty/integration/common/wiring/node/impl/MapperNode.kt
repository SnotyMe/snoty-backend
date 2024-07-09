package me.snoty.integration.common.wiring.node.impl

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
	val engine: String,
	val fields: Map<String, String>
) : NodeSettings

class MapperNodeHandler(override val nodeHandlerContext: NodeHandlerContext) : NodeHandler {
	override val settingsClass: KClass<out NodeSettings> = MapperSettings::class
	override val position: NodePosition = NodePosition.MIDDLE

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		val config: MapperSettings = node.getConfig()
		val data: Document = input.get()

		val mappedData = Document()
		// TODO: templating engine
		config.fields.forEach { (key, value) ->
			var result = value
			for (field in data) {
				result = result.replace("%${field.key}%", field.value.toString())
			}
			mappedData[key] = result
		}

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
