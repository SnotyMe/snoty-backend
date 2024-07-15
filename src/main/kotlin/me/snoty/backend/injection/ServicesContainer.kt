package me.snoty.backend.injection

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.superclasses

interface ServicesContainer {
	fun <T : Any> register(clazz: KClass<T>, instance: T)
	fun <T : Any> register(instance: T)
	fun <T : Any> get(clazz: KClass<T>): T
}

/**
 * Typical CDI services container
 */
class ServicesContainerImpl(block: ServicesContainer.() -> Unit) : ServicesContainer {
	private val services = mutableMapOf<KClass<*>, Any>()

	init {
		block()
	}

	override fun <T : Any> register(clazz: KClass<T>, instance: T) {
		services[clazz] = instance
	}

	override fun <T : Any> register(instance: T) {
		val clazz = instance::class
		val superclasses = clazz.superclasses - Any::class
		val registerForClass = when {
			superclasses.isEmpty() -> clazz
			superclasses.size == 1 -> superclasses.first()
			else -> throw IllegalStateException("Class to register for cannot be inferred for $clazz")
		}
		services[registerForClass] = instance
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> get(clazz: KClass<T>): T {
		val service = services[clazz]
		if (service != null) return service as T

		for (superclass in clazz.allSuperclasses) {
			val serviceBySuperclass = services[clazz]
			if (serviceBySuperclass != null) return serviceBySuperclass as T
		}

		throw IllegalStateException("Couldn't find an instance for $clazz")
	}
}

inline fun <reified T : Any> ServicesContainer.get(): T = get(T::class)
