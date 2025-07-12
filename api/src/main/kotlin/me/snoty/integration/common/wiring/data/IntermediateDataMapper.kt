package me.snoty.integration.common.wiring.data

import kotlin.reflect.KClass

/**
 * @param IM Intermediate data type - does NOT have something to do with the input / output it can handle
 */
interface IntermediateDataMapper<IM : IntermediateData> {
	/**
	 * Priority of the mapper. Higher value means higher priority.
	 */
	val priority: Int
	fun supports(clazz: KClass<*>): Boolean
	val intermediateDataClass: KClass<IM>

	fun <R : Any> deserialize(intermediateData: IM, clazz: KClass<R>): R

	fun <R : Any> deserializeUnsafe(intermediateData: IntermediateData, clazz: KClass<R>): R {
		@Suppress("UNCHECKED_CAST")
		return deserialize(intermediateData as IM, clazz)
	}

	fun <R : Any> serialize(data: R): IM
}
