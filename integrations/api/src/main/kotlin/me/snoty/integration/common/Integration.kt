package me.snoty.integration.common

import io.ktor.server.routing.*
import me.snoty.backend.User
import kotlin.reflect.KClass


interface Integration {
	val name: String
	val settingsType: KClass<out IntegrationSettings>
	val fetcher: IntegrationFetcher<*>

	fun start()
	fun schedule(user: User, settings: IntegrationSettings)
	fun setup(user: User, settings: IntegrationSettings): Long

	fun routes(routing: Route) {
		// default implementation does nothing
	}
}

interface IntegrationFactory {
	fun create(context: IntegrationContext): Integration
}
