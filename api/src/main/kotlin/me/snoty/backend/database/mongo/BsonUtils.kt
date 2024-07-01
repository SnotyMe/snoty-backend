package me.snoty.backend.database.mongo

import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

// the joys of mongodb
fun <T : Any> CodecRegistry.encode(value: T): Document {
	val document = BsonDocument()
	val writer = BsonDocumentWriter(document)
	@Suppress("UNCHECKED_CAST")
	val codec = get(value::class.java) as Codec<T>
	codec.encode(writer, value, EncoderContext.builder().build())

	val documentCodec = DocumentCodec()

	return documentCodec.decode(BsonDocumentReader(document), DecoderContext.builder().build())
}


// must be inlined to correctly create the context
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> CodecRegistry.decode(clazz: KClass<T>, document: Document): T {
	return get(clazz.java)
		.decode(
			BsonDocumentReader(document.toBsonDocument()),
			DecoderContext.builder().build()
		)
}
