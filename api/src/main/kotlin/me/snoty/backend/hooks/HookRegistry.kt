package me.snoty.backend.hooks

import org.koin.core.annotation.Single
import kotlin.reflect.KClass

interface HookRegistry {
	fun <D : Any, L : LifecycleHook<D>> registerHook(clazz: KClass<D>, hook: L)

	fun <T : Any> executeHooks(clazz: KClass<T>, data: T)
}

@Single
class HookRegistryImpl : HookRegistry {
	private val hooks = mutableMapOf<KClass<*>, MutableList<LifecycleHook<*>>>()

	override fun <D : Any, L : LifecycleHook<D>> registerHook(clazz: KClass<D>, hook: L) {
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

inline fun <reified D : Any, reified H : LifecycleHook<D>> HookRegistry.registerHook(hook: H)
	= registerHook(D::class, hook)
