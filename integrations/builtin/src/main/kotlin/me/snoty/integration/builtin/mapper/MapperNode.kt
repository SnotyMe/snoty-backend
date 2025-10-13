package me.snoty.integration.builtin.mapper

import kotlinx.serialization.Serializable
import me.snoty.backend.utils.bson.encode
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.*
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.mapInputWithSettings
import me.snoty.integration.common.wiring.data.structOutput
import me.snoty.integration.common.wiring.logger
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Serializable
data class MapperSettings(
	override val name: String = "Mapper",
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
	position = NodePosition.MIDDLE,
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
		node: Node,
		input: Collection<IntermediateData>,
	) = mapInputWithSettings<Document, MapperSettings>(input, node) { data, settings ->
		val mappedData = Document(data.mapValues { (_, value) ->
			val packageName = value.javaClass.packageName
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
