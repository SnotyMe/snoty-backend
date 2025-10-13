package me.snoty.integration.builtin.diff

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.logger
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.toList
import me.snoty.backend.utils.NULL_UUID
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.utils.bson.getIdAsString
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.checksum
import me.snoty.integration.common.diff.diff
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.get
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRouteFactory
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.slf4j.Logger

private val SINGLETON_ID = NULL_UUID.toString()

abstract class DiffNodeHandler(
	private val entityStateService: EntityStateService,
	nodeRouteFactory: NodeRouteFactory,
	codecRegistry: CodecRegistry,
) : NodeHandler {
	init {
		nodeRouteFactory("state", method = HttpMethod.Get, verifyUser = true) { node ->
			// scuffed code to encode states to json using bson

			data class States(val states: List<EntityState>)

			val states = States(entityStateService.getLastStates(node._id).toList())

			val mapped = codecRegistry.encode(states).toJson()

			call.respondText(mapped, ContentType.Application.Json)
		}

		nodeRouteFactory("clear", method = HttpMethod.Post, verifyUser = true) { node ->
			entityStateService.delete(node)
			call.respond(HttpStatusCode.OK)
		}
	}

	context(_: NodeHandleContext)
	suspend fun handleStatesAndDiff(slf4jLogger: Logger, node: Node, input: NodeInput, excludedFields: Collection<String>): Pair<Data, Changes> {
		val logger = KotlinLogging.logger(slf4jLogger)

		val newData = input
			.map { it.get<Document>() }
			// clone the document to avoid modifying the original
			.map { Document(it) }
			.mapNotNull { document ->
				val id = document.getIdAsString() ?: let {
					if (input.size == 1) {
						// if we have only one document, we can assume it's a singleton and use the SINGLETON_ID
						document["id"] = SINGLETON_ID
						SINGLETON_ID
					} else {
						// if we have multiple documents, we can't use the id of the node so we skip this document
						logger.warn { "Document has no id field, skipping..." }
						return@mapNotNull null
					}
				}
				id to document
			}
			.toMap()

		val rawOldStates: Map<String, EntityState> = entityStateService.getLastStates(node._id)
			.fold(mutableMapOf()) { acc, state ->
				acc[state.id] = state
				acc
			}
		
		val (oldStates, changes) = processStates(rawOldStates, Data(newData), excludedFields)

		entityStateService.updateStates(node._id, changes.values)
		val allData = newData + oldStates
			.filterKeys { it !in newData }
			.mapValues { it.value.state }

		return Data(allData) to Changes(changes)
	}

	@JvmInline
	value class States(private val states: Map<String, EntityState>)
		: Map<String, EntityState> by states

	@JvmInline
	value class Data(private val data: Map<String, Document>)
		: Map<String, Document> by data

	@JvmInline
	value class Changes(private val changes: Map<String, EntityStateService.EntityStateUpdate>)
		: Map<String, EntityStateService.EntityStateUpdate> by changes
}

private fun Document.removeAll(fields: Collection<String>) {
	for (field in fields) {
		this.remove(field)
	}
}

internal fun processStates(
	oldStates: Map<String, EntityState>,
	newData: DiffNodeHandler.Data,
	excludedFields: Collection<String>
): Pair<DiffNodeHandler.States, DiffNodeHandler.Changes> {
	val fullOldStates = oldStates
	val oldStates = oldStates
		// remove excluded fields to not consider them in the diff
		.mapValues { (_, it) ->
			val newState = Document(it.state)
			newState.removeAll(excludedFields)
			it.copy(
				state = newState,
				checksum = newState.checksum()
			)
		}

	val createAndUpdate = newData
		.mapValues { (id, newState) ->
			val oldState = oldStates[id]
			val newDocumentCleaned = Document(newState)
			// remove excluded fields to not consider them in the diff
			newDocumentCleaned.removeAll(excludedFields)
			val diff = newDocumentCleaned.diff(oldState)

			EntityStateService.EntityStateUpdate(EntityState(id, newState), diff)
		}
	val delete = fullOldStates
		// we only care about deleted entities
		.filterKeys { it !in newData }
		.mapValues { (_, oldState) ->
			// `null.diff` results in `DiffResult.Deleted`
			EntityStateService.EntityStateUpdate(oldState, null.diff(oldState))
		}

	val allUpdates = createAndUpdate + delete

	return DiffNodeHandler.States(fullOldStates) to DiffNodeHandler.Changes(allUpdates)
}
