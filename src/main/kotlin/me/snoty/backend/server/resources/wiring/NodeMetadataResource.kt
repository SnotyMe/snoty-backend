package me.snoty.backend.server.resources.wiring

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import me.snoty.backend.server.plugins.respondCaching
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.template.NodeMetadataFeatureFlags
import me.snoty.integration.common.wiring.node.template.NodeTemplateRegistry
import me.snoty.backend.server.koin.get as getDependency

fun Routing.nodeMetadataResource() {
	val featureFlags: NodeMetadataFeatureFlags = getDependency()
	val json: Json = getDependency()
	metadataEndpoint(featureFlags, json)
	templateEndpoint(featureFlags, json)
}

private fun Routing.metadataEndpoint(featureFlags: NodeMetadataFeatureFlags, json: Json) {
	val logger = KotlinLogging.logger {}
	val nodeRegistry: NodeRegistry = getDependency()

	@Serializable
	data class NodeDescription(val descriptor: NodeDescriptor, val metadata: NodeMetadata)

	fun computeMetadatas(): JsonElement {
		logger.debug { "Computing node metadata" }
		val nodeDescriptions = nodeRegistry.getMetadata().map { (descriptor, metadata) ->
			NodeDescription(descriptor, metadata)
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
	}
}

private fun Routing.templateEndpoint(featureFlags: NodeMetadataFeatureFlags, json: Json) {
	val logger = KotlinLogging.logger {}
	val nodeTemplateRegistry: NodeTemplateRegistry = getDependency()

	@Serializable
	data class NodeTemplates(val descriptor: NodeDescriptor, val templates: Map<String, String>)

	fun computeTemplates(): JsonElement {
		logger.debug { "Computing node templates" }
		val nodeTemplates = nodeTemplateRegistry.getAllTemplates().map { (descriptor, templates) ->
			NodeTemplates(descriptor, templates.associate {
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
