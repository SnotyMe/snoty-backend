package me.snoty.integration.builtin.diff.injector

import kotlinx.serialization.Serializable
import me.snoty.integration.builtin.diff.DiffNodeHandler
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Serializable
data class DiffInjectorSettings(
	override val name: String = "Diff Injector",
	val excludeFields: List<String>,
) : NodeSettings

@RegisterNode(
	type = "diffinjector",
	subsystem = Subsystem.PROCESSOR,
	displayName = "Diff Injector",
	position = NodePosition.MIDDLE,
	settingsType = DiffInjectorSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
)
@Single
class DiffInjectorNodeHandler(
	entityStateService: EntityStateService,
) : DiffNodeHandler(entityStateService) {
	context(NodeHandleContext)
	override suspend fun process(
		logger: Logger,
		node: Node,
		input: Collection<IntermediateData>,
	): NodeOutput {
		val settings = node.getConfig<DiffInjectorSettings>()

		val (newData, newStates) = handleStatesAndDiff(logger, node, input, settings.excludeFields)
		val items = newData
			.map { (id, ogDoc) ->
				// clone to avoid referencing self
				Document(ogDoc)
					.append("diff", newStates[id]?.diffResult)
			}

		return iterableStructOutput(items)
	}
}
