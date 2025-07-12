package me.snoty.integration.common.wiring.data

import org.koin.core.annotation.Single
import kotlin.reflect.KClass

interface IntermediateDataMapperRegistry {
	operator fun <T : IntermediateData> get(clazz: KClass<out T>): IntermediateDataMapper<T>

	fun getFirstCompatibleMapper(clazz: KClass<*>): IntermediateDataMapper<out IntermediateData>
}

@Single
class IntermediateDataMapperRegistryImpl(dataMappers: List<IntermediateDataMapper<*>>) : IntermediateDataMapperRegistry {
	private val allMappers = dataMappers.sortedByDescending { it.priority }

	private val mappers = allMappers
		.reversed() // later elements overwrite earlier ones -> opposite order of the list
		.associateBy { it.intermediateDataClass }

	private val dataMapperCache: MutableMap<KClass<*>, IntermediateDataMapper<out IntermediateData>> = mutableMapOf()

	@Suppress("UNCHECKED_CAST")
	override operator fun <T : IntermediateData> get(clazz: KClass<out T>): IntermediateDataMapper<T> =
		mappers[clazz] as? IntermediateDataMapper<T> ?: throw noMapper(clazz)

	override fun getFirstCompatibleMapper(clazz: KClass<*>): IntermediateDataMapper<out IntermediateData> = dataMapperCache.getOrPut(clazz) {
		allMappers.firstOrNull { it.supports(clazz) }
			?: throw noMapper(clazz)
	}

	private fun noMapper(clazz: KClass<out Any>) = IllegalStateException("No mapper found for $clazz")
}
