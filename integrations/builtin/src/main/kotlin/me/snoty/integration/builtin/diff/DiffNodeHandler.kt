package me.snoty.integration.builtin.diff

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.logger
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.toList
import me.snoty.backend.database.mongo.encode
import me.snoty.backend.database.mongo.getIdAsString
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.diff
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRouteFactory
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.slf4j.Logger


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

	suspend fun NodeHandleContext.handleStatesAndDiff(slf4jLogger: Logger, node: Node, input: NodeInput, excludedFields: Collection<String>): Pair<Data, States> {
		val logger = KotlinLogging.logger(slf4jLogger)

		val newData = input
			.map { get<Document>(it) }
			// clone the document to avoid modifying the original
			.map { Document(it) }
			.mapNotNull { document ->
				val id = document.getIdAsString() ?: let {
					logger.warn { "Document has no id field, skipping..." }
					return@mapNotNull null
				}
				id to document
			}
			.toMap()

		val oldStates = entityStateService.getLastStates(node._id)
			.toList()
			.associateBy { it.id }
		val newStates = newData
			// remove excluded fields to not consider them in the diff
			.onEach { (_, document) ->
				excludedFields.forEach { field -> document.remove(field) }
			}
			.mapValues { (id, newState) ->
				val oldState = oldStates[id]
				val diff = newState.diff(oldState)

				logger.debug { "Entity $id was $diff" }

				EntityStateService.EntityStateUpdate(EntityState(id, newState), diff)
			}

		entityStateService.updateStates(node._id, newStates.values)
		val allStates = newStates + oldStates
			// we only care about deleted entities
			.filterKeys { it !in newData }
			.mapValues { (id, _) ->
				val oldState = oldStates[id] ?: error("Old state not found for entity $id")
				EntityStateService.EntityStateUpdate(oldState, null.diff(oldState))
			}
		val allData = newData + oldStates
			.filterKeys { it !in newData }
			.mapValues { it.value.state }

		return Data(allData) to States(allStates)
	}

	@JvmInline
	value class Data(private val data: Map<String, Document>)
		: Map<String, Document> by data

	@JvmInline
	value class States(private val states: Map<String, EntityStateService.EntityStateUpdate>)
		: Map<String, EntityStateService.EntityStateUpdate> by states
}
