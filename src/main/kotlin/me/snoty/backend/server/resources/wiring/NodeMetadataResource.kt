package me.snoty.backend.server.resources.wiring

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import me.snoty.backend.server.plugins.respondCaching
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.registry.NodeRegistry
import me.snoty.backend.wiring.node.template.NodeMetadataFeatureFlags
import me.snoty.backend.wiring.node.template.NodeTemplateRegistry
import me.snoty.core.node.NodeType
import org.koin.ktor.ext.get as getDependency

fun Route.nodeMetadataResource() = route("node/metadata") {
	val featureFlags: NodeMetadataFeatureFlags = getDependency()
	val json: Json = getDependency()
	metadataEndpoint(featureFlags, json)
	templateEndpoint(featureFlags, json)
}.describe {
	tag("node-metadata")
}

private fun Route.metadataEndpoint(featureFlags: NodeMetadataFeatureFlags, json: Json) {
	val logger = KotlinLogging.logger {}
	val nodeRegistry: NodeRegistry = getDependency()

	@Serializable
	data class NodeDescription(val type: NodeType, val metadata: NodeMetadata)

	fun computeMetadatas(): JsonElement {
		logger.debug { "Computing node metadata" }
		val nodeDescriptions = nodeRegistry.getMetadata().map { (nodeType, metadata) ->
			NodeDescription(nodeType, metadata)
		}
		return json.encodeToJsonElement(nodeDescriptions)
	}

	val cachedDescription = computeMetadatas()
	get {
		val description = when {
			featureFlags.cacheNodeMetadata -> cachedDescription
			else -> computeMetadatas()
		}

		call.respondCaching(description)
	}.describe {
		responses {
			HttpStatusCode.OK {
				schema = jsonSchema<List<NodeDescription>>()
			}
		}
	}
}

private fun Route.templateEndpoint(featureFlags: NodeMetadataFeatureFlags, json: Json) {
	val logger = KotlinLogging.logger {}
	val nodeTemplateRegistry: NodeTemplateRegistry = getDependency()

	@Serializable
	data class NodeTemplates(val type: NodeType, val templates: Map<String, String>)

	fun computeTemplates(): JsonElement {
		logger.debug { "Computing node templates" }
		val nodeTemplates = nodeTemplateRegistry.getAllTemplates().map { (nodeType, templates) ->
			NodeTemplates(nodeType, templates.associate {
				it.name to it.template
			})
		}
		return json.encodeToJsonElement(nodeTemplates)
	}

	val cachedTemplates = computeTemplates()
	get("template") {
		val nodeTemplates = when {
			featureFlags.cacheNodeTemplates -> cachedTemplates
			else -> computeTemplates()
		}

		call.respondCaching(nodeTemplates)
	}
}
