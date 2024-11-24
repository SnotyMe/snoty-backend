package me.snoty.backend.database.mongo

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.bson.BsonDocumentWrapper
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

class DocumentSerializer(private val codecRegistry: CodecRegistry) : KSerializer<Document> {
	@Serializable
	private abstract class MapAnyMap : Map<String, Any?>

	override val descriptor: SerialDescriptor = MapAnyMap.serializer().descriptor

	@OptIn(ExperimentalSerializationApi::class)
	override fun serialize(encoder: Encoder, value: Document) {
		if (encoder !is JsonEncoder) {
			encoder.encodeString(value.toJson())
			return
		}
		val jsonString = BsonDocumentWrapper.asBsonDocument(value, codecRegistry).toJson()
		encoder.encodeJsonElement(JsonUnquotedLiteral(jsonString))
	}

	override fun deserialize(decoder: Decoder): Document {
		val content = when (decoder) {
			is JsonDecoder -> decoder.decodeJsonElement().jsonObject.toString()
			else -> decoder.toString()
		}
		return Document.parse(content)
	}
}

@Single
@Named("documentModule")
fun provideDocumentModuleProvider(codecRegistry: CodecRegistry) = SerializersModule {
	contextual(DocumentSerializer(codecRegistry))
}
