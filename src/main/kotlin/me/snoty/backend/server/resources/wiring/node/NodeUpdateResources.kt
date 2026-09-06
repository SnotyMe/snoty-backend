package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import me.snoty.backend.utils.*
import me.snoty.core.node.Node
import me.snoty.integration.common.config.NodePatch
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.node.NodePosition
import org.slf4j.event.Level
import java.util.*

@Serializable
data class NodePatchRequest(
	val name: String? = null,
	val position: NodePosition? = null,
	val logLevel: JsonElement? = JsonNull,
	val settings: JsonElement? = null,
)

fun Route.nodeUpdate(nodeService: NodeService) {
	put("{id}") {
		val node = getPersonalNodeOrNull() ?: return@put

		val settingsJson = call.receive<JsonElement>()
		val settings = deserializeSettings(node.descriptor, settingsJson) ?: return@put

		val result = nodeService.updateSettings(node, settings)

		call.respondServiceResult(result)
	}

	patch("{id}") {
		val node = getPersonalNodeOrNull() ?: return@patch
		val request: NodePatchRequest = call.receive()

		val logLevel: Optional<Level?>? = when (val level = request.logLevel) {
			JsonNull -> null // not set in the request -> don't change the level
			null -> optionalOf(null) // explicitly set to null in the request -> unset the level
			else -> {
				if (level !is JsonPrimitive || !level.jsonPrimitive.isString) {
					return@patch call.respondStatus(BadRequestException("Expected ${Node::logLevel.name} to be a string"))
				}

				val logLevelString = level.jsonPrimitive.contentOrNull?.uppercase() ?: return@patch
				letOrNull { Level.valueOf(logLevelString) }?.let(::optionalOf)
					?: return@patch call.respondStatus(BadRequestException("Couldn't parse ${Node::logLevel.name} level"))
			}
		}

		val settings = request.settings?.let { settingsJson ->
			deserializeSettings(node.descriptor, settingsJson) ?: return@patch
		}

		val patch = NodePatch(
			name = request.name,
			position = request.position,
			logLevel = logLevel,
			settings = settings,
		)
		nodeService.patch(node, patch)

		call.respond(HttpStatusCode.NoContent)
	}
}
