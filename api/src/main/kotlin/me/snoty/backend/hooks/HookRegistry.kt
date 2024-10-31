package me.snoty.backend.hooks

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

interface HookRegistry {
	fun <D : Any, H : LifecycleHook<D>> registerHook(clazz: KClass<H>, hook: H)

	suspend fun <D : Any, H : LifecycleHook<D>> executeHooks(clazz: KClass<H>, data: D)
}

@Single
class HookRegistryImpl : HookRegistry {
	private val hooks = mutableMapOf<KClass<*>, MutableList<LifecycleHook<*>>>()

	override fun <D : Any, H : LifecycleHook<D>> registerHook(clazz: KClass<H>, hook: H) {
		val list = hooks.getOrPut(clazz) { mutableListOf() }
		list.add(hook)
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun <D : Any, H : LifecycleHook<D>> executeHooks(clazz: KClass<H>, data: D) = coroutineScope {
		// removing cleans up the map
		val list = hooks[clazz] as? List<LifecycleHook<D>> ?: return@coroutineScope
		list.map {
			async {
				it(data)
			}
		}.awaitAll()
		Unit
	}
}

inline fun <reified H : LifecycleHook<*>> HookRegistry.register(hook: H)
	= registerHook(H::class, hook)
