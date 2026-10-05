package me.snoty.backend.wiring.data.impl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import me.snoty.backend.utils.bson.decode
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.IntermediateDataMapper
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

data class BsonIntermediateData(override val value: Document, val documentCodec: DocumentCodec) : IntermediateData {
	override fun toString(): String = value.toJson(documentCodec)
}

@Single
class BsonIntermediateDataMapper(private val codecRegistry: CodecRegistry, private val documentCodec: DocumentCodec) : IntermediateDataMapper<BsonIntermediateData>, KSerializer<BsonIntermediateData> {
	override val priority = 1000
	override fun supports(clazz: KClass<*>) = clazz == Document::class
	override val intermediateDataClass = BsonIntermediateData::class

	override fun <R : Any> deserialize(intermediateData: BsonIntermediateData, clazz: KClass<R>): R {
		if (clazz == Document::class) {
			// we've verified that `R` is `Document` thanks to the class parameter
			@Suppress("UNCHECKED_CAST")
			return intermediateData.value as R
		}

		return codecRegistry.decode(clazz, intermediateData.value)
	}

	override fun <R : Any> serialize(data: R) = BsonIntermediateData(
		when (data) {
			is Document -> data
			else -> codecRegistry.encode(data)
		},
		documentCodec
	)

	override val descriptor = buildClassSerialDescriptor("BsonIntermediateData") {
		element("value", serialDescriptor<String>())
	}

	override fun serialize(encoder: Encoder, value: BsonIntermediateData) = encoder.encodeStructure(descriptor) {
		encodeStringElement(descriptor, 0, value.value.toJson(value.documentCodec))
	}

	override fun deserialize(decoder: Decoder): BsonIntermediateData = decoder.decodeStructure(descriptor) {
		val json = decodeStringElement(descriptor, 0)
		val document = Document.parse(json, documentCodec)
		BsonIntermediateData(document, documentCodec)
	}
}
