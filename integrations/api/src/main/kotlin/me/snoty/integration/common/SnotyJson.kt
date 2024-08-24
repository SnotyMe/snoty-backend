package me.snoty.integration.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.backend.utils.UUIDSerializer
import me.snoty.integration.common.utils.kotlinxSerializersModule
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import org.koin.core.KoinApplication
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.module.Module
import java.util.*

/**
 * Base JSON configuration for Snoty.
 * Does NOT support [NodeSettings][me.snoty.integration.common.wiring.node.NodeSettings]!
 */
val BaseSnotyJson = snotyJson {}

fun snotyJson(block: JsonBuilder.() -> Unit) = Json {
	serializersModule = kotlinxSerializersModule + SerializersModule {
		contextual(UUID::class, UUIDSerializer)
	}
	ignoreUnknownKeys = true
	encodeDefaults = true
	block()
}

private fun List<SerializersModule>.merge(): SerializersModule
	= this.reduce { acc, serializersModule -> acc + serializersModule }

/*
@Single
fun provideSerializersModules(nodeHandlerContributors: NodeHandlerContributorList): SerializersModule =
	nodeHandlerContributors.flatMap<NodeHandlerContributor, SerializersModule> {
		it.koin.getAll<SerializersModule>()
	}.merge()
*/

@Single
fun provideSerializersModule(): SerializersModule = kotlinxSerializersModule

@Single
fun snotyJson(serializersModules: List<SerializersModule>) = snotyJson {
	serializersModule += serializersModules.merge()
}
