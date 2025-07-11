package me.snoty.integration.builtin.split

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.*
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
	@FieldHidden
	@FieldDefaultValue("REPLACE_ROOT")
	val behavior: SplitBehavior = SplitBehavior.REPLACE_ROOT,
) : NodeSettings

enum class SplitBehavior {
	@DisplayName("Replace Root")
	@FieldDescription("Creates a new output object only consisting of the list item.")
	REPLACE_ROOT,

	@DisplayName("Replace Key")
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
		val splitData = data[key] ?: throw IllegalArgumentException("Key '$key' not found in input data")

		if (splitData !is List<*>) {
			throw IllegalArgumentException("Data at key '$key' must be a list")
		}

		splitData.filterNotNull().map { item ->
			when (settings.behavior) {
				SplitBehavior.REPLACE_ROOT -> serializePolymorphic(item)
				SplitBehavior.REPLACE_KEY -> {
					val document = Document(data)
					document.put(key, item)
					serializeBson(document)
				}
			}
		}
	}
}
