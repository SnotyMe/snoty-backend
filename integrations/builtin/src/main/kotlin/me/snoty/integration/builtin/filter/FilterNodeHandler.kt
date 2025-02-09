package me.snoty.integration.builtin.filter

import kotlinx.serialization.Serializable
import liqp.TemplateParser
import me.snoty.integration.builtin.utils.encodeObjects
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.Language
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Serializable
data class FilterSettings(
	override val name: String = "Filter",
	
	@Language("liquid")
	@FieldDescription("Liquid template returning `true` (keep element) or `false` (drop element)")
	val expression: String,
) : NodeSettings

@RegisterNode(
	name = "filter",
	displayName = "Filter",
	settingsType = FilterSettings::class,
	position = NodePosition.MIDDLE,
	inputType = Any::class,
	outputType = Any::class,
)
@Single
class FilterNodeHandler(
	private val codecRegistry: CodecRegistry,
) : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>
	): NodeOutput {
		val settings: FilterSettings = node.getConfig()
		val template = TemplateParser.DEFAULT.parse(settings.expression)
		
		// no modifications happen to the original data
		val result = input.filter {
			val data = get<Document>(it).encodeObjects(codecRegistry)
			val rendered = template.render(data).trim()
			rendered.trim().toBooleanStrict()
		}
		
		return result
	}
}