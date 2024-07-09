package me.snoty.integration.common.wiring

import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData

context(IntermediateDataMapperRegistryContext, EmitNodeOutputContext)
suspend fun <T : Any> iterableStructOutput(
	context: FetchContext,
	producer: suspend FetchContext.() -> Iterable<T>
): Unit = producer(context)
	// serialize arbitrary object into BsonIntermediateData
	.forEach { emitSerialized(it) }

context(IntermediateDataMapperRegistryContext, EmitNodeOutputContext)
suspend fun <T : Any> structOutput(
	producer: suspend () -> T
) {
	val data = producer()
	emitSerialized(data)
}

context(IntermediateDataMapperRegistryContext, EmitNodeOutputContext)
private suspend fun emitSerialized(data: Any) {
	// serialize arbitrary object into BsonIntermediateData
	val serialized = intermediateDataMapperRegistry[BsonIntermediateData::class]
		.serialize(data)
	emit(serialized)
}

context(IntermediateDataMapperRegistryContext)
inline fun <reified T : Any> IntermediateData.get()

	= intermediateDataMapperRegistry[this@get::class]
		.deserializeUnsafe(this@get, T::class)
