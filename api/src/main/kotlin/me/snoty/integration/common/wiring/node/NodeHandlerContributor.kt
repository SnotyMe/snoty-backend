package me.snoty.integration.common.wiring.node

import org.koin.core.module.Module
import kotlin.reflect.KClass

interface NodeHandlerContributor {
	val metadata: String
	val settingsClass: KClass<out NodeSettings>?
		get() = null

	val nodeHandlerClass: KClass<out NodeHandler>
	val koinModules: List<Module>
	val koinScope: KClass<*>?
		get() = null
}
