package me.snoty.node.split

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.schema.FieldDefaultValue
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.schema.FieldHidden
import me.snoty.backend.utils.bson.getByPath
import me.snoty.backend.utils.bson.setByPath
import me.snoty.backend.wiring.data.*
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class SplitSettings(
	@FieldDescription("The key on which to split the input data. Other elements will be shared between the individual outputs.")
	val key: String,
	@FieldDescription("If false, the Node will throw an error instead of looping through object values.")
	@FieldDefaultValue("true")
	val loopThroughObjectKeys: Boolean = true,
	@FieldHidden
	@FieldDefaultValue("REPLACE_ROOT")
	val behavior: SplitBehavior = SplitBehavior.REPLACE_ROOT,
) : NodeSettings

enum class SplitBehavior {
	@FieldDescription("Creates a new output object only consisting of the list item.")
	REPLACE_ROOT,

	@FieldDescription("Replaces the specified key with the list item, keeping other elements in the output object.")
	REPLACE_KEY
}

@RegisterNode(
	name = "split",
	displayName = "Split",
	icon = Icon(name = "lucide-split"),
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
	settingsType = SplitSettings::class,
	stereotype = NodeStereotype.MIDDLE,
)
@Single
class SplitNodeHandler : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: Collection<IntermediateData>
	): NodeOutput = mapInputWithSettings<Document, SplitSettings>(input, node) { data, settings ->
		val key = settings.key
		val splitData = data.getByPath(key) ?: throw IllegalArgumentException("Key '$key' not found in input data")

		val list = when (splitData) {
			is List<*> -> splitData.filterNotNull()
			is Document if settings.loopThroughObjectKeys -> splitData.values
			else -> throw IllegalArgumentException("Data at key '$key' must be a list or loopThroughObjectKeys must be true")
		}

		list.map { item ->
			when (settings.behavior) {
				SplitBehavior.REPLACE_ROOT -> serializePolymorphic(item)
				SplitBehavior.REPLACE_KEY -> {
					val document = Document(data)
					document.setByPath(key, item)
					serializeBson(document)
				}
			}
		}
	}
}
