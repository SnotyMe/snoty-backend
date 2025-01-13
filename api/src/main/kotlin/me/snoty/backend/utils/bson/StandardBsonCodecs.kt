package me.snoty.backend.utils.bson

import com.mongodb.MongoClientSettings
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

fun standardCodecRegistry(): CodecRegistry = MongoClientSettings.getDefaultCodecRegistry()

@Single
fun provideCodecRegistry(codecRegistryProvider: List<CodecRegistryProvider>, codecs: List<Codec<*>>): CodecRegistry =
	CodecRegistries.fromRegistries(
		listOf(CodecRegistries.fromCodecs(*codecs.toTypedArray()))
		+ codecRegistryProvider.sortedBy(CodecRegistryProvider::priority)
			.reversed()
			.map(CodecRegistryProvider::registry)
		+ standardCodecRegistry()
	)

fun provideCodecRegistry(vararg codecRegistryProvider: CodecRegistryProvider) =
	provideCodecRegistry(codecRegistryProvider.toList(), emptyList())
