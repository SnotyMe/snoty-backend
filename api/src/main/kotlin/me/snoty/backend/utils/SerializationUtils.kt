package me.snoty.backend.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * A hacky way to encode an object to a JSON string using the serializer of its class.
 * This allows users to serialize objects whose classes are not known at compile time.
 */
@OptIn(InternalSerializationApi::class)
fun <T : Any> Json.hackyEncodeToString(it: T) = encodeToString((it::class as KClass<T>).serializer(), it)
