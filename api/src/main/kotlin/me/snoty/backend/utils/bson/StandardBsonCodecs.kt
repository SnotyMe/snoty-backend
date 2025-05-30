package me.snoty.backend.utils.bson

import com.mongodb.MongoClientSettings
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

fun standardCodecRegistry(): CodecRegistry = MongoClientSettings.getDefaultCodecRegistry()

@Single
fun provideCodecRegistry(codecRegistryProvider: List<CodecRegistryProvider>): CodecRegistry =
	CodecRegistries.fromRegistries(
		codecRegistryProvider.sortedBy(CodecRegistryProvider::priority)
			.reversed()
			.map(CodecRegistryProvider::registry)
		+ standardCodecRegistry()
	)

fun provideCodecRegistry(vararg codecRegistryProvider: CodecRegistryProvider) =
	provideCodecRegistry(codecRegistryProvider.toList())

@Single
fun provideDocumentCodec(codecRegistry: CodecRegistry, bsonTypeClassMap: BsonTypeClassMap) =
	DocumentCodec(codecRegistry, bsonTypeClassMap)
