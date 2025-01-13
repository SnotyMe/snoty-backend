package me.snoty.backend.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
fun <T : Any> Json.hackyEncodeToString(it: T) = encodeToString((it::class as KClass<T>).serializer(), it)
