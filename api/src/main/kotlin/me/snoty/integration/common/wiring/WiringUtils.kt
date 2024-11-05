package me.snoty.integration.common.wiring

import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import org.slf4j.Logger
import kotlin.reflect.KClass

interface NodeHandleContext {
	val intermediateDataMapperRegistry: IntermediateDataMapperRegistry
	val logger: Logger
}

data class NodeHandleContextImpl(
	override val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	override val logger: Logger,
) : NodeHandleContext

fun <T : Any> NodeHandleContext.iterableStructOutput(
	items: Iterable<T>
): Collection<IntermediateData> = items
	// serialize arbitrary object into BsonIntermediateData
	.map { serializeBson(it) }

fun <T : Any> NodeHandleContext.structOutput(vararg data: T) = data.map { serializeBson(it) }

private fun NodeHandleContext.serializeBson(data: Any) = serialize(BsonIntermediateData::class, data)


fun <T : Any> NodeHandleContext.simpleOutput(vararg items: T): NodeOutput = items
	.map { serialize(SimpleIntermediateData::class, it) }


private fun <IM : IntermediateData, T : Any> NodeHandleContext.serialize(clazz: KClass<IM>, data: T): IM {
	val mapper = intermediateDataMapperRegistry[clazz]
	return mapper.serialize(data)
}

inline fun <reified T : Any> NodeHandleContext.get(intermediateData: IntermediateData)
	= intermediateDataMapperRegistry[intermediateData::class]
		.deserializeUnsafe(intermediateData, T::class)
