package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.database.mongo.decode
import me.snoty.backend.wiring.node.MongoSettingsService.Companion.logger
import me.snoty.integration.common.wiring.graph.MongoNode
import me.snoty.integration.common.wiring.node.InvalidNodeSettings
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

fun interface MongoSettingsService {
	fun lookup(node: MongoNode, settingsClassOverride: KClass<out NodeSettings>?): NodeSettings

	companion object {
		val logger = KotlinLogging.logger {}
	}
}

fun MongoSettingsService.lookupOrInvalid(node: MongoNode) = runCatching {
	lookup(node, null)
}.recover { e ->
	logger.error(e) { "Failed to lookup settings for node $node" }
	lookup(node, InvalidNodeSettings::class)
}.recover { e ->
	logger.error(e) { "Failed to lookup invalid settings for node $node" }
	InvalidNodeSettings(node.settings.getString(NodeSettings::name.name))
}.getOrThrow()

@Single
class MongoSettingsServiceImpl(private val nodeRegistry: NodeRegistry, private val codecRegistry: CodecRegistry) : MongoSettingsService {
	override fun lookup(node: MongoNode, settingsClassOverride: KClass<out NodeSettings>?): NodeSettings {
		val actualSettingsClass = settingsClassOverride
			?: nodeRegistry.getMetadata(node.descriptor).settingsClass
		return codecRegistry.decode(actualSettingsClass, node.settings)
	}
}
