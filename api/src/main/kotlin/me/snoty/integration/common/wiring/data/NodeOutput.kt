package me.snoty.integration.common.wiring.data

import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import kotlin.reflect.KClass

typealias NodeOutput = Collection<IntermediateData>

// region BSON
/**
 * Serialize arbitrary object into BsonIntermediateData.
 * Documents will be passed as-is without serialization.
 */
context(ctx: NodeHandleContext)
fun <T : Any> iterableStructOutput(
	items: Iterable<T>
): Collection<IntermediateData> = items
	// serialize arbitrary object into BsonIntermediateData
	.map { serializeBson(it) }

context(ctx: NodeHandleContext)
fun <T : Any> structOutput(vararg data: T) = data.map { serializeBson(it) }

context(ctx: NodeHandleContext)
fun serializeBson(data: Any) = serialize(BsonIntermediateData::class, data)
//endregion


// region Simple
context(ctx: NodeHandleContext)
fun <T : Any> simpleOutput(vararg items: T): NodeOutput = items
	.map { serialize(SimpleIntermediateData::class, it) }
// endregion


// region Generic
context(ctx: NodeHandleContext)
private fun <IM : IntermediateData, T : Any> serialize(clazz: KClass<IM>, data: T): IM {
	val mapper = ctx.intermediateDataMapperRegistry[clazz]
	return mapper.serialize(data)
}
// endregion

// region Polymorphic
context(ctx: NodeHandleContext)
fun polymorphicOutput(vararg data: Any) = data.map { serializePolymorphic(it) }

context(ctx: NodeHandleContext)
fun serializePolymorphic(data: Any) = ctx
	.intermediateDataMapperRegistry
	.getFirstCompatibleMapper(data::class)
	.serialize(data)
// endregion
