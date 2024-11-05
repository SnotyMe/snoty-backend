package me.snoty.integration.common.diff

import org.bson.Document
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.Codec
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

const val STATE_CODEC_REGISTRY = "stateCodecRegistry"

@Single
@Named(STATE_CODEC_REGISTRY)
fun provideStateCodecRegistry(bsonTypeClassMap: BsonTypeClassMap, codecRegistry: CodecRegistry): CodecRegistry = CodecRegistries.fromRegistries(
	CodecRegistries.fromProviders(
		object : CodecProvider {
			@Suppress("UNCHECKED_CAST")
			override fun <T : Any> get(clazz: Class<T?>?, registry: CodecRegistry?): Codec<T>? = when (clazz) {
				Document::class.java -> DocumentCodec(registry, bsonTypeClassMap)
				else -> null
			} as? Codec<T>?
		}
	),
	codecRegistry,
)
