package me.snoty.backend.hooks

import kotlin.reflect.KClass

interface HookRegistry {
	fun <T : Any> registerHook(clazz: KClass<T>, hook: LifecycleHook<T>)

	fun <T : Any> executeHooks(clazz: KClass<T>, data: T)
}

class HookRegistryImpl : HookRegistry {
	private val hooks = mutableMapOf<KClass<*>, MutableList<LifecycleHook<*>>>()

	override fun <T : Any> registerHook(clazz: KClass<T>, hook: LifecycleHook<T>) {
		val list = hooks.getOrPut(clazz) { mutableListOf() }
		list.add(hook)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> executeHooks(clazz: KClass<T>, data: T) {
		// removing cleans up the map
		val list = hooks.remove(clazz) as? List<LifecycleHook<T>> ?: return
		list.forEach { it(data) }
	}
}

inline fun <reified T : Any> HookRegistry.registerHook(noinline hook: LifecycleHook<T>)
	= registerHook(T::class, hook)
