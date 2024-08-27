package me.snoty.integration.common.wiring

import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import kotlin.reflect.KClass

interface NodeHandleContext {
	val intermediateDataMapperRegistry: IntermediateDataMapperRegistry
	val flowCollector: EmitNodeOutputContext
}

data class NodeHandleContextImpl(
	override val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	override val flowCollector: EmitNodeOutputContext
) : NodeHandleContext

context(NodeHandleContext)
suspend fun <T : Any> iterableStructOutput(
	context: FetchContext,
	producer: suspend FetchContext.() -> Iterable<T>
): Unit = producer(context)
	// serialize arbitrary object into BsonIntermediateData
	.forEach { emitBson(it) }

context(NodeHandleContext)
suspend fun <T : Any> structOutput(producer: suspend () -> T) = emitBson(producer())

context(NodeHandleContext)
private suspend fun emitBson(data: Any) = emitSerialized(BsonIntermediateData::class, data)


context(NodeHandleContext)
suspend fun <T : Any> simpleOutput(producer: suspend () -> T) = emitSerialized(SimpleIntermediateData::class, producer())


context(NodeHandleContext)
private suspend fun <IM : IntermediateData, T : Any> emitSerialized(clazz: KClass<IM>, data: T) {
	val mapper = intermediateDataMapperRegistry[clazz]
	flowCollector.emit(mapper.serialize(data))
}

context(NodeHandleContext)
inline fun <reified T : Any> IntermediateData.get()
	= intermediateDataMapperRegistry[this@get::class]
		.deserializeUnsafe(this@get, T::class)
