package me.snoty.backend.wiring.node

import me.snoty.backend.utils.bson.decode
import me.snoty.backend.wiring.node.registry.NodeRegistry
import me.snoty.core.node.NodeType
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

interface NodeSettingsDeserializationService {
	fun deserializeOrInvalid(nodeType: NodeType, nodeSettings: Document): NodeSettings
}

@Single
class NodeSettingsDeserializationServiceImpl(private val nodeRegistry: NodeRegistry, private val codecRegistry: CodecRegistry) : NodeSettingsDeserializationService {
	override fun deserializeOrInvalid(nodeType: NodeType, nodeSettings: Document): NodeSettings =
		tryDeserializeNodeSettings(nodeType, nodeRegistry) {
			codecRegistry.decode(it, nodeSettings)
		}
}
