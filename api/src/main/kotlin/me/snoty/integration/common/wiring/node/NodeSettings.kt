package me.snoty.integration.common.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.reflect.KClass

interface NodeSettings {
	/**
	 * Name of the node
	 */
	val name: String
}

@Serializable
data class EmptyNodeSettings(
	override val name: String = "Empty"
) : NodeSettings

@Serializable
data class InvalidNodeSettings(
	override val name: String,
	private val _invalid: Boolean = true
) : NodeSettings

object EmptyNodeSettingsCodec : Codec<EmptyNodeSettings> {
	override fun getEncoderClass(): Class<EmptyNodeSettings> = EmptyNodeSettings::class.java
	override fun encode(writer: BsonWriter?, value: EmptyNodeSettings?, encoderContext: EncoderContext?) {}
	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): EmptyNodeSettings
		= EmptyNodeSettings()
}

fun tryDeserializeNodeSettings(nodeDescriptor: NodeDescriptor, nodeRegistry: NodeRegistry, deserialize: (clazz: KClass<out NodeSettings>) -> NodeSettings): NodeSettings {
	val metadata = runCatching { nodeRegistry.getMetadata(nodeDescriptor) }.getOrNull()
	val logger = KotlinLogging.logger {}

	fun recover() = runCatching {
		deserialize(InvalidNodeSettings::class)
	}.getOrElse {
		logger.error(it) { "Failed to deserialize to ${InvalidNodeSettings::class} for node $nodeDescriptor" }
		InvalidNodeSettings(name = nodeDescriptor.name)
	}

	if (metadata == null) {
		logger.error { "Failed to get metadata for node $nodeDescriptor" }
		return recover()
	}

	return runCatching {
		deserialize(metadata.settingsClass)
	}.getOrElse { e ->
		logger.error(e) { "Failed to deserialize to ${metadata.settingsClass} for node $nodeDescriptor" }
		recover()
	}
}
