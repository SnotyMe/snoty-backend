package me.snoty.integration.common.diff

import me.snoty.backend.database.mongo.apiCodecModule
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

abstract class AbstractDiffTest(
	codecModules: List<Codec<*>>
) {
	val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
		CodecRegistries.fromCodecs(codecModules),
		integrationsApiCodecModule(),
		apiCodecModule()
	)

	fun encodeDocument(obj: IUpdatableEntity<*>): Document {
		val document = BsonDocument()
		val changeCodec = codecRegistry.get(Fields::class.java)
		changeCodec.encode(
			BsonDocumentWriter(document),
			obj.fields,
			EncoderContext.builder().build()
		)
		val fields = codecRegistry.get(Document::class.java).decode(document.asBsonReader(), DecoderContext.builder().build())
		return fields
	}
}
