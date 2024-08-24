package me.snoty.backend.integration.utils

import me.snoty.backend.database.mongo.decode
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

fun interface MongoSettingsService {
	fun lookup(node: GraphNode): NodeSettings
}

@Single
class MongoSettingsServiceImpl(private val nodeRegistry: NodeRegistry, private val codecRegistry: CodecRegistry) : MongoSettingsService {
	override fun lookup(node: GraphNode): NodeSettings {
		val handler = nodeRegistry.lookupHandler(node.descriptor) ?: throw IllegalArgumentException("No handler found for node ${node.descriptor}")
		return codecRegistry.decode(handler.metadata.settingsClass, node.settings)
	}
}
