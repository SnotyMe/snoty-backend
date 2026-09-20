package me.snoty.node.diff.unchangedfilter

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.iterableStructOutput
import me.snoty.backend.wiring.node.NodeHandleContext
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.backend.wiring.node.logger
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.backend.wiring.node.routing.NodeRouteFactory
import me.snoty.backend.wiring.node.state.DiffResult
import me.snoty.backend.wiring.node.state.EntityStateService
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import me.snoty.node.diff.DiffNodeHandler
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Serializable
data class UnchangedFilterSettings(
	val excludeFields: List<String>,
) : NodeSettings

@RegisterNode(
	name = "unchangedfilter",
	displayName = "Unchanged Filter",
	stereotype = NodeStereotype.MIDDLE,
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
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
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
