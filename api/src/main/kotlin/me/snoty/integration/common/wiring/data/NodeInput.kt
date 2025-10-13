package me.snoty.integration.common.wiring.data

import me.snoty.backend.utils.letOrNull
import me.snoty.integration.common.wiring.NodeHandleContext

typealias NodeInput = Collection<IntermediateData>

// region getters
context(ctx: NodeHandleContext)
inline fun <reified T : Any> IntermediateData.getOrNull() = letOrNull { get<T>() }

context(ctx: NodeHandleContext)
inline fun <reified T : Any> IntermediateData.get() = ctx
	.intermediateDataMapperRegistry[this::class]
	.deserializeUnsafe(this, T::class)
// endregion
