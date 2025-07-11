package me.snoty.integration.common.wiring

import me.snoty.backend.utils.letOrNull
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

/**
 * Serialize arbitrary object into BsonIntermediateData.
 * Documents will be passed as-is without serialization.
 */
fun <T : Any> NodeHandleContext.iterableStructOutput(
	items: Iterable<T>
): Collection<IntermediateData> = items
	// serialize arbitrary object into BsonIntermediateData
	.map { serializeBson(it) }

fun <T : Any> NodeHandleContext.structOutput(vararg data: T) = data.map { serializeBson(it) }

fun NodeHandleContext.serializeBson(data: Any) = serialize(BsonIntermediateData::class, data)


fun <T : Any> NodeHandleContext.simpleOutput(vararg items: T): NodeOutput = items
	.map { serialize(SimpleIntermediateData::class, it) }

fun NodeHandleContext.polymorphicOutput(vararg data: Any) = data.map { serializePolymorphic(it) }

fun NodeHandleContext.serializePolymorphic(data: Any) = intermediateDataMapperRegistry.getFirstCompatibleMapper(data::class).serialize(data)

private fun <IM : IntermediateData, T : Any> NodeHandleContext.serialize(clazz: KClass<IM>, data: T): IM {
	val mapper = intermediateDataMapperRegistry[clazz]
	return mapper.serialize(data)
}

inline fun <reified T : Any> NodeHandleContext.getOrNull(intermediateData: IntermediateData) = letOrNull { get<T>(intermediateData) }

inline fun <reified T : Any> NodeHandleContext.get(intermediateData: IntermediateData)
	= intermediateDataMapperRegistry[intermediateData::class]
		.deserializeUnsafe(intermediateData, T::class)
