package me.snoty.backend.database.mongo

import org.bson.BsonDocumentReader
import org.bson.Document
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

// must be inlined to correctly create the context
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> CodecRegistry.decode(clazz: KClass<T>, document: Document): T {
	return get(clazz.java)
		.decode(
			BsonDocumentReader(document.toBsonDocument()),
			DecoderContext.builder().build()
		)
}
