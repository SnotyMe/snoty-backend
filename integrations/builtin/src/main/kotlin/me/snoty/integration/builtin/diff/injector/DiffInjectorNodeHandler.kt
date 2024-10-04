package me.snoty.integration.builtin.diff.injector

import kotlinx.serialization.Serializable
import me.snoty.integration.builtin.diff.DiffNodeHandler
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class DiffInjectorSettings(
	override val name: String = "Diff Injector",
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
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>,
	): NodeOutput {
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
					.append("diff", allStates[id]?.diffResult)
			}

		return iterableStructOutput(items)
	}
}
