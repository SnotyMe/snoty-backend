package me.snoty.integration.builtin.diff

import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.backend.database.mongo.getIdAsString
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.diff
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Serializable
data class UnchangedFilterSettings(
	override val name: String = "Unchanged Filter",
	val excludeFields: List<String>,
) : NodeSettings

@RegisterNode(
	type = "unchangedfilter",
	subsystem = Subsystem.FILTER,
	displayName = "Unchanged Filter",
	position = NodePosition.MIDDLE,
	settingsType = UnchangedFilterSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
)
@Single
class UnchangedFilterNodeHandler(
	private val entityStateService: EntityStateService,
) : NodeHandler {
	context(NodeHandleContext)
	override suspend fun process(
		logger: Logger,
		node: Node,
		input: Collection<IntermediateData>,
	): NodeOutput {
		val settings = node.getConfig<UnchangedFilterSettings>()

		val newData = input
			.map { it.get<Document>() }
			// clone the document to avoid modifying the original
			.map { Document(it) }
			.mapNotNull { document ->
				val id = document.getIdAsString() ?: let {
					logger.warn("Document has no _id field, skipping...")
					return@mapNotNull null
				}
				id to document
			}

		val oldStates = entityStateService.getLastStates(node._id)
			.toList()

		val newStates = newData
			// remove excluded fields to not consider them in the diff
			.onEach { (_, document) ->
				settings.excludeFields.forEach { field -> document.remove(field) }
			}
			.associate { (id, newState) ->
				val oldState = oldStates.find { it.id == id }
				val diff = newState.diff(oldState)

				EntityState(id, newState) to diff
			}

		entityStateService.updateStates(node._id, newStates)

		return iterableStructOutput(newStates.keys)
	}
}
