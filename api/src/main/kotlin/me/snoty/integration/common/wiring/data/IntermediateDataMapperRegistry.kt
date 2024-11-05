package me.snoty.integration.common.wiring.data

import org.koin.core.annotation.Single
import kotlin.reflect.KClass

interface IntermediateDataMapperRegistry {
	operator fun <T : IntermediateData> set(clazz: KClass<T>, mapper: IntermediateDataMapper<T>)
	operator fun <T : IntermediateData> get(clazz: KClass<out T>): IntermediateDataMapper<T>
}

@Single
class IntermediateDataMapperRegistryImpl(dataMappers: List<IntermediateDataMapper<*>>) : IntermediateDataMapperRegistry {
	private val mappers = mutableMapOf<KClass<*>, IntermediateDataMapper<*>>()

	init {
		dataMappers.forEach {
			mappers[it.intermediateDataClass] = it
		}
	}

	override operator fun <T : IntermediateData> set(clazz: KClass<T>, mapper: IntermediateDataMapper<T>) {
		mappers[clazz] = mapper
	}

	@Suppress("UNCHECKED_CAST")
	override operator fun <T : IntermediateData> get(clazz: KClass<out T>): IntermediateDataMapper<T> {
		return mappers[clazz] as? IntermediateDataMapper<T> ?: throw noMapper(clazz)
	}

	private fun noMapper(clazz: KClass<out Any>) = IllegalStateException("No mapper found for $clazz")
}
