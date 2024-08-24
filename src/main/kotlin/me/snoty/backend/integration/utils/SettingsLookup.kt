package me.snoty.backend.integration.utils

import me.snoty.backend.database.mongo.decode
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

typealias SettingsLookup = (GraphNode) -> NodeSettings

@Single
fun mongoSettingsLookup(nodeRegistry: NodeRegistry, codecRegistry: CodecRegistry): SettingsLookup = { node ->
	val handler = nodeRegistry.lookupHandler(node.descriptor) ?: throw IllegalArgumentException("No handler found for node ${node.descriptor}")
	codecRegistry.decode(handler.settingsClass, node.settings)
}
