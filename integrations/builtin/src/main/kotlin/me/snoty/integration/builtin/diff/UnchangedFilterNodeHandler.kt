package me.snoty.integration.builtin.diff

import kotlinx.serialization.Serializable
import me.snoty.backend.database.mongo.getIdAsString
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.diff
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.IntermediateData
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
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val settings: UnchangedFilterSettings = node.getConfig()

		// clone to avoid modifying the original document
		// may be done by default in the future
		val document: Document = Document(input.get()).apply {
			settings.excludeFields.forEach { field -> this.remove(field) }
		}

		val id = document.getIdAsString() ?: let {
			logger.error("Document has no id field, skipping")
			return
		}

		val lastState = entityStateService.getLastState(node._id, id)

		val diff = document.diff(lastState)

		if (diff is DiffResult.Unchanged) {
			logger.debug("Document $id is unchanged, skipping")
			return
		}

		entityStateService.updateState(node._id, document, diff)

		structOutput { document }
	}
}
