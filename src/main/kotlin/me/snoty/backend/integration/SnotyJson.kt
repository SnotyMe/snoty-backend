package me.snoty.backend.integration

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.backend.injection.getFromAllScopes
import me.snoty.backend.utils.snotyJson
import org.koin.core.Koin
import org.koin.core.annotation.Single

private fun List<SerializersModule>.merge(): SerializersModule
	= this.reduce { acc, serializersModule -> acc + serializersModule }

@Single
fun snotyJson(koin: Koin, serializersModules: List<SerializersModule>) = snotyJson {
	val modules = koin.getFromAllScopes<SerializersModule>()
	serializersModule += (serializersModules + modules).merge()
}
