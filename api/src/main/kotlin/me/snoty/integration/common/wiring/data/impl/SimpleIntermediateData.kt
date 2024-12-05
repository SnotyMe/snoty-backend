package me.snoty.integration.common.wiring.data.impl

import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapper
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

data class SimpleIntermediateData(
	override val value: Any,
	val valueType: KClass<*> = value::class
) : IntermediateData

@Single
class SimpleIntermediateDataMapper : IntermediateDataMapper<SimpleIntermediateData> {
	override val intermediateDataClass = SimpleIntermediateData::class

	@Suppress("UNCHECKED_CAST")
	override fun <R : Any> deserialize(intermediateData: SimpleIntermediateData, clazz: KClass<R>): R {
		val value = intermediateData.value
		require(clazz.isInstance(value)) { "Value is not an instance of $clazz" }

		return value as R
	}

	override fun <R : Any> serialize(data: R): SimpleIntermediateData {
		return SimpleIntermediateData(data, data::class)
	}
}