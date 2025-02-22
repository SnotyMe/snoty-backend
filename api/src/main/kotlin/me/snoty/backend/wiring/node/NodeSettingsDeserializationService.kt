package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.utils.bson.decode
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService.Companion.logger
import me.snoty.integration.common.wiring.node.InvalidNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

fun interface NodeSettingsDeserializationService {
	fun deserialize(nodeDescriptor: NodeDescriptor, nodeSettings: Document, settingsClassOverride: KClass<out NodeSettings>?): NodeSettings

	companion object {
		val logger = KotlinLogging.logger {}
	}
}

fun NodeSettingsDeserializationService.deserializeOrInvalid(nodeDescriptor: NodeDescriptor, nodeSettings: Document) = runCatching {
	deserialize(nodeDescriptor, nodeSettings, null)
}.recover { e ->
	logger.error(e) { "Failed to lookup settings for node $nodeDescriptor" }
	deserialize(nodeDescriptor, nodeSettings, InvalidNodeSettings::class)
}.recover { e ->
	logger.error(e) { "Failed to lookup invalid settings for node $nodeDescriptor" }
	InvalidNodeSettings(nodeSettings.getString(NodeSettings::name.name))
}.getOrThrow()

@Single
class NodeSettingsDeserializationServiceImpl(private val nodeRegistry: NodeRegistry, private val codecRegistry: CodecRegistry) : NodeSettingsDeserializationService {
	override fun deserialize(nodeDescriptor: NodeDescriptor, nodeSettings: Document, settingsClassOverride: KClass<out NodeSettings>?): NodeSettings {
		val actualSettingsClass = settingsClassOverride
			?: nodeRegistry.getMetadata(nodeDescriptor).settingsClass
		return codecRegistry.decode(actualSettingsClass, nodeSettings)
	}
}
