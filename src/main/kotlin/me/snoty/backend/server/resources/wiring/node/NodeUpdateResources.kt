package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.respondServiceResult
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.Node
import org.slf4j.event.Level
import java.util.*

fun Route.nodeUpdate(nodeService: NodeService) {
	put("{id}") {
		val node = getPersonalNodeOrNull() ?: return@put

		val settingsJson = call.receive<JsonElement>()
		val settings = deserializeSettings(node.descriptor, settingsJson) ?: return@put

		val result = nodeService.updateSettings(node._id, settings)

		call.respondServiceResult(result)
	}

	patch("{id}") {
		val node = getPersonalNodeOrNull() ?: return@patch
		val request = call.receive<JsonElement>()

		if (request !is JsonObject) {
			return@patch call.respondStatus(BadRequestException("Expected a JSON object"))
		}

		request[Node::settings.name]?.let { settingsJson ->
			val settings = deserializeSettings(node.descriptor, settingsJson) ?: return@patch
			nodeService.updateSettings(node._id, settings)
		}

		request[Node::logLevel.name]?.let { level ->
			if (level is JsonNull) {
				// explicitly set to null in the request -> unset the level
				nodeService.updateLogLevel(node._id, null)
				return@let
			}

			if (level !is JsonPrimitive || !level.jsonPrimitive.isString) {
				return@patch call.respondStatus(BadRequestException("Expected ${Node::logLevel.name} to be a string"))
			}

			val logLevelString = level.jsonPrimitive.contentOrNull ?: return@patch
			val logLevel = runCatching { Level.valueOf(logLevelString.uppercase(Locale.ROOT)) }.getOrNull()
				?: return@patch call.respondStatus(BadRequestException("Couldn't parse ${Node::logLevel.name} level"))
			nodeService.updateLogLevel(node._id, logLevel)
		}

		call.respond(HttpStatusCode.NoContent)
	}
}
