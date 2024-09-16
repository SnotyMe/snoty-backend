package me.snoty.integration.common.wiring

import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import kotlin.reflect.KClass

interface NodeHandleContext {
	val intermediateDataMapperRegistry: IntermediateDataMapperRegistry
}

data class NodeHandleContextImpl(
	override val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
) : NodeHandleContext

context(NodeHandleContext)
fun <T : Any> iterableStructOutput(
	items: Iterable<T>
): Collection<IntermediateData> = items
	// serialize arbitrary object into BsonIntermediateData
	.map { serializeBson(it) }

context(NodeHandleContext)
fun <T : Any> structOutput(vararg data: T) = data.map { serializeBson(it) }

context(NodeHandleContext)
private fun serializeBson(data: Any) = serialize(BsonIntermediateData::class, data)


context(NodeHandleContext)
fun <T : Any> simpleOutput(vararg items: T): NodeOutput = items
	.map { serialize(SimpleIntermediateData::class, it) }


context(NodeHandleContext)
private fun <IM : IntermediateData, T : Any> serialize(clazz: KClass<IM>, data: T): IM {
	val mapper = intermediateDataMapperRegistry[clazz]
	return mapper.serialize(data)
}

context(NodeHandleContext)
inline fun <reified T : Any> IntermediateData.get()
	= intermediateDataMapperRegistry[this@get::class]
		.deserializeUnsafe(this@get, T::class)
