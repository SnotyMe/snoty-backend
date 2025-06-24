package me.snoty.integration.builtin.diff.unchangedfilter

import kotlinx.serialization.Serializable
import me.snoty.integration.builtin.diff.DiffNodeHandler
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeRouteFactory
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Serializable
data class UnchangedFilterSettings(
	override val name: String = "Unchanged Filter",
	val excludeFields: List<String>,
) : NodeSettings

@RegisterNode(
	name = "unchangedfilter",
	displayName = "Unchanged Filter",
	position = NodePosition.MIDDLE,
	settingsType = UnchangedFilterSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
)
@Single
class UnchangedFilterNodeHandler(
	entityStateService: EntityStateService,
	nodeRouteFactory: NodeRouteFactory,
	codecRegistry: CodecRegistry,
) : DiffNodeHandler(entityStateService, nodeRouteFactory, codecRegistry) {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>,
	): NodeOutput {
		val settings = node.getConfig<UnchangedFilterSettings>()

		val (newData, newStates) = handleStatesAndDiff(logger, node, input, settings.excludeFields)
		val items = newData
			.filterNot { (id, _) ->
				newStates[id]?.diffResult == DiffResult.Unchanged
			}.map { it.value }

		return iterableStructOutput(items)
	}
}
