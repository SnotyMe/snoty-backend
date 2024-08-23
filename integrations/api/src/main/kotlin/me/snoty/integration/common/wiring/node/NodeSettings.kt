package me.snoty.integration.common.wiring.node

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

interface NodeSettings {
	/**
	 * Name of the node
	 */
	val name: String
}

data class EmptyNodeSettings(
	override val name: String = "Empty"
) : NodeSettings

object EmptyNodeSettingsCodec : Codec<EmptyNodeSettings> {
	override fun getEncoderClass(): Class<EmptyNodeSettings> = EmptyNodeSettings::class.java
	override fun encode(writer: BsonWriter?, value: EmptyNodeSettings?, encoderContext: EncoderContext?) {}
	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): EmptyNodeSettings
		= EmptyNodeSettings()
}
