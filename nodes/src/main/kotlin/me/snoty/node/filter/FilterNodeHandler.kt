package me.snoty.node.filter

import kotlinx.serialization.Serializable
import liqp.TemplateParser
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.schema.Language
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.get
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class FilterSettings(
	@Language("liquid")
	@FieldDescription("Liquid template returning `true` (keep element) or `false` (drop element)")
	val expression: String,
) : NodeSettings

@RegisterNode(
	name = "filter",
	displayName = "Filter",
	icon = Icon(name = "lucide-funnel"),
	settingsType = FilterSettings::class,
	stereotype = NodeStereotype.MIDDLE,
	inputType = Any::class,
	outputType = Any::class,
)
@Single
class FilterNodeHandler : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: Collection<IntermediateData>
	): NodeOutput {
		val settings: FilterSettings = node.getConfig()
		val template = TemplateParser.DEFAULT.parse(settings.expression)
		
		// no modifications happen to the original data
		val result = input.filter {
			val data = it.get<Document>()
			val rendered = template.render(data).trim()
			rendered.trim().toBooleanStrict()
		}
		
		return result
	}
}
