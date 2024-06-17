package me.snoty.backend.integration.config.flow

import kotlin.reflect.KClass

data class NodeOutput<T : Any>(
	val name: String,
	val type: KClass<T>
)

data class NodeInput<T : Any>(
	val name: String,
	val type: KClass<T>
)
