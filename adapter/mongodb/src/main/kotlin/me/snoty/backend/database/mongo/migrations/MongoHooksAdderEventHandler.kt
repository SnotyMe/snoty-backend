package me.snoty.backend.database.mongo.migrations

import me.snoty.backend.events.EventHandler
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.PreBusinessStartupHook
import me.snoty.backend.hooks.register
import org.koin.core.annotation.Single

@Single
class MongoHooksAdderEventHandler : EventHandler {
	override fun handleInitializationEvent(hookRegistry: HookRegistry) {
		hookRegistry.register(PreBusinessStartupHook {
			it.get<MongoMigrator>().migrate()
		})
	}
}
