package me.snoty.integration.builtin.diff.uni

import kotlinx.serialization.Serializable
import me.snoty.integration.builtin.diff.injector.HasDiff
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.getNew
import me.snoty.integration.common.diff.getOld
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.EmptySchema
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class UniDiffSettings(
	override val name: String = "Uni Diff",
	val fields: List<String>,
) : NodeSettings

data class UniDiff(
	val unidiff: EmptySchema
)

@RegisterNode(
	name = "unidiff",
	displayName = "Uni Diff",
	position = NodePosition.MIDDLE,
	settingsType = UniDiffSettings::class,
	inputType = HasDiff::class,
	outputType = UniDiff::class,
)
@Single
class UniDiffHandler : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>
	): NodeOutput {
		val settings = node.settings as UniDiffSettings
		val output = input
			.associateWith {
				val hasDiff = get<HasDiff>(it)

				val diff = hasDiff.diff

				if (diff !is DiffResult.Updated) {
					logger.info("Entity ${hasDiff.id} is not updated; it was ${diff::class.simpleName}")
					return@associateWith emptyMap()
				}

				settings.fields
					.filter { key ->
						if (!diff.change.containsKey(key)) {
							logger.debug("Entity {} is updated, but field {} is not present in the diff, which only has {}", hasDiff.id, key, diff.change)
							false
						}
						true
					}
					.associateWith { key ->
						val (old, new) = (diff.change as Document).get(key, Document::class.java).run {
							Change(
								old = this.getOld(),
								new = this.getNew(),
							)
						}
						logger.debug("Entity {} is updated, field {} has changed from {} to {}", hasDiff.id, key, old, new)

						computeDiff(old, new)
					}
			}

		return output.map { (input, items) ->
			// clone to avoid interference with other nodes
			val document = Document(get<Document>(input))
			document["unidiff"] = Document(items)
			// TODO: clone on creation in BsonIntermediateData
			BsonIntermediateData(document)
		}
	}
}
