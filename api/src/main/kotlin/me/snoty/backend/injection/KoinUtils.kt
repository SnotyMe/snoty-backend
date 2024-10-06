package me.snoty.backend.injection

import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi

@OptIn(KoinInternalApi::class)
inline fun <reified T : Any> Koin.getFromAllScopes(): List<T> = this.scopeRegistry.scopeDefinitions.map {
	this.getOrCreateScope(it.value, it)
}.flatMap {
	it.getAll()
}
