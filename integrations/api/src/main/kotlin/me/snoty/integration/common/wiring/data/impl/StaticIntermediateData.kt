package me.snoty.integration.common.wiring.data.impl

import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapper
import kotlin.reflect.KClass

data class StaticIntermediateData(
	override val value: Any,
	val valueType: KClass<*> = value::class
) : IntermediateData

object StaticIntermediateDataMapper : IntermediateDataMapper<StaticIntermediateData> {
	override fun <R : Any> deserialize(intermediateData: StaticIntermediateData, clazz: KClass<R>): R {
		val value = intermediateData.value
		require(clazz.isInstance(value)) { "Value is not an instance of $clazz" }
		@Suppress("UNCHECKED_CAST")
		return value as R
	}

	override fun <R : Any> serialize(data: R): StaticIntermediateData {
		return StaticIntermediateData(data, data::class)
	}
}
