package me.snoty.integration.common

import me.snoty.backend.User
import kotlin.reflect.KClass


interface Integration {
	val name: String
	val settingsType: KClass<out Any>
	val fetcher: Fetcher<*>

	fun start()
	fun schedule(user: User, settings: Any)
}

interface IntegrationFactory {
	fun create(context: IntegrationContext): Integration
}
