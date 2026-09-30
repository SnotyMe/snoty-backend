package me.snoty.backend.wiring.data.impl

import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.IntermediateDataMapper
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

data object EmptyIntermediateData : IntermediateData {
	override val value: Any
		get() = error("EmptyIntermediateData has no value")
}

@Single
class EmptyIntermediateDataMapper : IntermediateDataMapper<EmptyIntermediateData> {
	override val priority = 0

	override fun supports(clazz: KClass<*>) = false

	override val intermediateDataClass = EmptyIntermediateData::class

	override fun <R : Any> deserialize(
		intermediateData: EmptyIntermediateData,
		clazz: KClass<R>
	): R = throw IllegalStateException("Cannot deserialize EmptyIntermediateData")

	override fun <R : Any> serialize(data: R) = EmptyIntermediateData
}
