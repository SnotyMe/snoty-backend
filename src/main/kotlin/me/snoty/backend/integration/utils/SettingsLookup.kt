package me.snoty.backend.integration.utils

import me.snoty.backend.database.mongo.decode
import me.snoty.integration.common.wiring.graph.MongoNode
import me.snoty.integration.common.wiring.node.InvalidNodeSettings
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

fun interface MongoSettingsService {
	fun lookup(node: MongoNode, settingsClassOverride: KClass<out NodeSettings>?): NodeSettings
}

fun MongoSettingsService.lookupOrInvalid(node: MongoNode) = runCatching {
	lookup(node, null)
}.getOrNull() ?: lookup(node, InvalidNodeSettings::class)

@Single
class MongoSettingsServiceImpl(private val nodeRegistry: NodeRegistry, private val codecRegistry: CodecRegistry) : MongoSettingsService {
	override fun lookup(node: MongoNode, settingsClassOverride: KClass<out NodeSettings>?): NodeSettings {
		val actualSettingsClass = settingsClassOverride
			?: nodeRegistry.getMetadata(node.descriptor).settingsClass
		return codecRegistry.decode(actualSettingsClass, node.settings)
	}
}
