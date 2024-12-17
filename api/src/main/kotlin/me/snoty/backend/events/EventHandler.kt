package me.snoty.backend.events

import me.snoty.backend.hooks.HookRegistry

interface EventHandler {
	fun handleInitializationEvent(hookRegistry: HookRegistry) {}
}
