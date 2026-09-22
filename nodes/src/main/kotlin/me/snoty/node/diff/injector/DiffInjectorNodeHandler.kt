package me.snoty.node.diff.injector

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.schema.FieldDefaultValue
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.iterableStructOutput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.backend.wiring.node.routing.NodeRouteFactory
import me.snoty.backend.wiring.node.state.DiffResult
import me.snoty.backend.wiring.node.state.EntityStateService
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import me.snoty.node.diff.DiffNodeHandler
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Serializable
data class DiffInjectorSettings(
	val excludeFields: List<String>,
	@FieldDefaultValue("true")
	val emitCreated: Boolean = true,
	@FieldDefaultValue("true")
	val emitUpdated: Boolean = true,
	@FieldDefaultValue("true")
	val emitDeleted: Boolean = true,
	@FieldDefaultValue("false")
	val emitUnchanged: Boolean = false,
) : NodeSettings

data class HasDiff(
	val id: String,
	val diff: DiffResult,
)

@RegisterNode(
	name = "diffinjector",
	displayName = "Diff Injector",
	icon = Icon(name = "lucide-file-diff"),
	stereotype = NodeStereotype.MIDDLE,
	settingsType = DiffInjectorSettings::class,
	inputType = EmptySchema::class,
	outputType = EmptySchema::class,
)
@ReceiveEmptyInput
@Single
class DiffInjectorNodeHandler(
	entityStateService: EntityStateService,
	nodeRouteFactory: NodeRouteFactory,
	codecRegistry: CodecRegistry,
) : DiffNodeHandler(entityStateService, nodeRouteFactory, codecRegistry) {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput {
		val settings = node.getConfig<DiffInjectorSettings>()

		val (newData, allStates) = handleStatesAndDiff(logger, node, input, settings.excludeFields)
		val items = newData
			.filter { (id, _) ->
				when (allStates[id]?.diffResult) {
					is DiffResult.Created -> settings.emitCreated
					is DiffResult.Updated -> settings.emitUpdated
					is DiffResult.Deleted -> settings.emitDeleted
					is DiffResult.Unchanged -> settings.emitUnchanged
					else -> {
						logger.error("DiffResult is null for entity $id")
						false
					}
				}
			}
			.map { (id, ogDoc) ->
				// clone to avoid referencing self
				Document(ogDoc)
					.append(HasDiff::diff.name, allStates[id]?.diffResult)
			}

		return iterableStructOutput(items)
	}
}
