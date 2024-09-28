package me.snoty.backend.server.resources.wiring

import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.template.NodeTemplateRegistry
import me.snoty.backend.server.koin.get as getDependency

fun Routing.nodeMetadataResource() {
	val nodeRegistry: NodeRegistry = getDependency()
	get {
		@Serializable
		data class NodeDescription(val descriptor: NodeDescriptor, val metadata: NodeMetadata)

		val nodeDescriptions = nodeRegistry.getMetadata().map { (descriptor, metadata) ->
			NodeDescription(descriptor, metadata)
		}

		call.respond(nodeDescriptions)
	}

	val nodeTemplateRegistry: NodeTemplateRegistry = getDependency()
	get("/template")    {
		@Serializable
		data class NodeTemplates(val descriptor: NodeDescriptor, val templates: Map<String, String>)

		val nodeTemplates = nodeTemplateRegistry.getAllTemplates().map { (descriptor, templates) ->
			NodeTemplates(descriptor, templates.associate {
				it.name to it.template
			})
		}

		call.respond(nodeTemplates)
	}
}
