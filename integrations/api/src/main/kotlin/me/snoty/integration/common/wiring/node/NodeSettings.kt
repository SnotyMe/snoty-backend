package me.snoty.integration.common.wiring.node

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

interface NodeSettings

class EmptyNodeSettings : NodeSettings

object EmptyNodeSettingsCodec : Codec<EmptyNodeSettings> {
	override fun getEncoderClass(): Class<EmptyNodeSettings> = EmptyNodeSettings::class.java
	override fun encode(writer: BsonWriter?, value: EmptyNodeSettings?, encoderContext: EncoderContext?) {}
	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): EmptyNodeSettings
		= EmptyNodeSettings()
}
