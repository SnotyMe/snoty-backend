package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.utils.bson.decode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.tryDeserializeNodeSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

interface NodeSettingsDeserializationService {
	fun deserializeOrInvalid(nodeDescriptor: NodeDescriptor, nodeSettings: Document): NodeSettings

	companion object {
		val logger = KotlinLogging.logger {}
	}
}

@Single
class NodeSettingsDeserializationServiceImpl(private val nodeRegistry: NodeRegistry, private val codecRegistry: CodecRegistry) : NodeSettingsDeserializationService {
	override fun deserializeOrInvalid(nodeDescriptor: NodeDescriptor, nodeSettings: Document): NodeSettings =
		tryDeserializeNodeSettings(nodeDescriptor, nodeRegistry) {
			codecRegistry.decode(it, nodeSettings)
		}
}
