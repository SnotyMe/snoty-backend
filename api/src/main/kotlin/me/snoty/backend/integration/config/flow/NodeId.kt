package me.snoty.backend.integration.config.flow

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

typealias NodeId = @Serializable(NodeIdSerializer::class) ObjectId

object NodeIdSerializer : KSerializer<NodeId> {
	override val descriptor: SerialDescriptor = serialDescriptor<String>()

	override fun deserialize(decoder: Decoder): NodeId {
		return NodeId(decoder.decodeString())
	}

	override fun serialize(encoder: Encoder, value: NodeId) {
		encoder.encodeString(value.toString())
	}
}
