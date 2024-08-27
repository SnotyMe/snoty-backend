package me.snoty.backend.integration

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.utils.kotlinxSerializersModule
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.annotation.Single

private fun List<SerializersModule>.merge(): SerializersModule
	= this.reduce { acc, serializersModule -> acc + serializersModule }

@Single
fun provideSerializersModule(): SerializersModule = kotlinxSerializersModule

@OptIn(KoinInternalApi::class)
@Single
fun snotyJson(koin: Koin, serializersModules: List<SerializersModule>) = snotyJson {
	// clone the set to avoid ConcurrentModificationException
	val scopeDefinitions = koin.scopeRegistry.scopeDefinitions.toSet()
	val modules = scopeDefinitions.flatMap {
		koin.getOrCreateScope(it.value, it)
			.getAll<SerializersModule>()
	}
	serializersModule += (serializersModules + modules).merge()
}
