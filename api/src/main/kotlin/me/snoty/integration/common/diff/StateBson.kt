package me.snoty.integration.common.diff

import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.DocumentCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

const val STATE_CODEC_REGISTRY = "stateCodecRegistry"

@Single
@Named(STATE_CODEC_REGISTRY)
fun provideStateCodecRegistry(bsonTypeClassMap: BsonTypeClassMap, codecRegistry: CodecRegistry): CodecRegistry = CodecRegistries.fromRegistries(
	CodecRegistries.fromProviders(DocumentCodecProvider(bsonTypeClassMap)),
	codecRegistry,
)
