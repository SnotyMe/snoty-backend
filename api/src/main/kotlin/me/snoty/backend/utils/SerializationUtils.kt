package me.snoty.backend.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import me.snoty.backend.wiring.node.EmptyNodeSettings
import me.snoty.backend.wiring.node.InvalidNodeSettings
import me.snoty.backend.wiring.node.NodeSettings
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

/**
 * A hacky way to encode an object to a JSON string using the serializer of its class.
 * This allows users to serialize objects whose classes are not known at compile time.
 */
@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
fun <T : Any> Json.hackyEncodeToString(it: T) = encodeToString((it::class as KClass<T>).serializer(), it)

val kotlinxSerializersModule = SerializersModule {
	polymorphic(NodeSettings::class) {
		subclass(EmptyNodeSettings::class)
		subclass(InvalidNodeSettings::class)
	}
}

@Single
fun provideSerializersModule(): SerializersModule = kotlinxSerializersModule
