package me.snoty.integration.builtin.split

import kotlinx.serialization.Serializable
import me.snoty.backend.utils.bson.getByPath
import me.snoty.backend.utils.bson.setByPath
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.FieldHidden
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.mapInputWithSettings
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.serializeBson
import me.snoty.integration.common.wiring.serializePolymorphic
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class SplitSettings(
	override val name: String = "Split",
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
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
	settingsType = SplitSettings::class,
	position = NodePosition.MIDDLE,
)
@Single
class SplitNodeHandler : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
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
