package me.snoty.backend.hooks

import kotlin.reflect.KClass

interface HookRegistry {
	fun <D : Any, H : LifecycleHook<D>> registerHook(clazz: KClass<H>, hook: H)

	suspend fun <D : Any, H : LifecycleHook<D>> executeHooks(clazz: KClass<H>, data: D)
}

inline fun <reified H : LifecycleHook<*>> HookRegistry.register(hook: H)
	= registerHook(H::class, hook)
