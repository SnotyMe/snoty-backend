package me.snoty.node.mapper

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.*
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.mapInputWithSettings
import me.snoty.backend.wiring.data.structOutput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Serializable
data class MapperSettings(
	val engine: MapperEngine,
	@FieldDescription("The fields to map - every key will be part of the output object")
	val fields: Map<String, @Language("liquid") String>,
	@FieldDescription("If true, the ID of the input object will be preserved in the output object")
	@FieldDefaultValue("true")
	val preserveId: Boolean = true,
	@FieldDescription("If true, every line of the output will be trimmed to remove surrounding non-visible characters. Useful for Liquid templates with indents.")
	@FieldDefaultValue("true")
	@FieldHidden
	val trim: Boolean = false, // set to false for backwards compatibility
	@FieldHidden
	val preserveFields: List<String> = emptyList(),
) : NodeSettings

@RegisterNode(
	name = "mapper",
	displayName = "Mapper",
	icon = Icon(name = "lucide-shuffle"),
	stereotype = NodeStereotype.MIDDLE,
	settingsType = MapperSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class
)
@Single
class MapperNodeHandler(
	private val codecRegistry: CodecRegistry,
) : NodeHandler, KoinComponent {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: NodeInput,
	) = mapInputWithSettings<Document, MapperSettings>(input, node) { data, settings ->
		val mappedData = Document(data.mapValues { (_, value) ->
			val packageName = value?.javaClass?.packageName ?: return@mapValues null
			if (packageName.startsWith("kotlin") || packageName.startsWith("java") || value::class == Document::class) value
			else codecRegistry.encode(value)
		})

		val result = settings.engine.template(logger, this@MapperNodeHandler, settings, mappedData)
		if (settings.trim) {
			result.trimAll()
		}

		structOutput(result)
	}
}
