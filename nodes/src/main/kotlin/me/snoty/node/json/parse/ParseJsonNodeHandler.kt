package me.snoty.node.json.parse

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.utils.bson.getByPath
import me.snoty.backend.utils.bson.parseJson
import me.snoty.backend.utils.bson.setByPath
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.mapInputWithSettings
import me.snoty.backend.wiring.data.structOutput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import org.bson.Document
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Serializable
data class ParseJsonNodeSettings(
	val fields: List<String>,
) : NodeSettings

@RegisterNode(
	displayName = "Parse JSON",
	icon = Icon(name = "lucide-braces"),
	name = "parsejson",
	stereotype = NodeStereotype.MIDDLE,
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
		node: NodeWithSettings,
		input: NodeInput,
	): NodeOutput = mapInputWithSettings<Document, ParseJsonNodeSettings>(input, node) { document, settings ->
		settings.fields.forEach { key ->
			when (val fieldData = document.getByPath(key)) {
				// already parsed
				is Document -> return@forEach
				is String -> {
					val parsed = parseJson(fieldData, codecRegistry, bsonTypeClassMap)
					document.setByPath(key, parsed)
				}
				else -> error("Field '$key' is not a string or document, cannot parse as JSON")
			}
		}

		structOutput(document)
	}
}
