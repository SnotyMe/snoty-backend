package me.snoty.backend.hooks

typealias LifecycleHook<T> = suspend (T) -> Unit
