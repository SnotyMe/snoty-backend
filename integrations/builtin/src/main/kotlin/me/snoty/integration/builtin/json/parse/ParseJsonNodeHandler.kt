package me.snoty.integration.builtin.json.parse

import kotlinx.serialization.Serializable
import me.snoty.backend.utils.bson.getByPath
import me.snoty.backend.utils.bson.setByPath
import me.snoty.integration.builtin.utils.parseJson
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.mapInputWithSettings
import me.snoty.integration.common.wiring.data.structOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Serializable
data class ParseJsonNodeSettings(
	override val name: String,
	val fields: List<String>,
) : NodeSettings

@RegisterNode(
	displayName = "Parse JSON",
	name = "parsejson",
	position = NodePosition.MIDDLE,
	settingsType = ParseJsonNodeSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
)
@Single
class ParseJsonNodeHandler(
	private val codecRegistry: CodecRegistry,
	private val bsonTypeClassMap: BsonTypeClassMap,
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: Node,
		input: Collection<IntermediateData>
	): NodeOutput = mapInputWithSettings<Document, ParseJsonNodeSettings>(input, node) { document, settings ->
		settings.fields.forEach { key ->
			val fieldData = document.getByPath(key)
			when (fieldData) {
				// already parsed
				is Document -> return@forEach
				is String -> {
					val parsed = fieldData.parseJson(codecRegistry, bsonTypeClassMap)
					document.setByPath(key, parsed)
				}
				else -> error("Field '$key' is not a string or document, cannot parse as JSON")
			}
		}

		structOutput(document)
	}
}
