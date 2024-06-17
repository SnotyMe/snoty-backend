package me.snoty.integration.common

import io.ktor.server.routing.*
import me.snoty.backend.User
import me.snoty.backend.integration.config.ConfigId
import me.snoty.integration.common.fetch.IntegrationFetcher
import org.bson.codecs.Codec
import kotlin.reflect.KClass


interface Integration {
	val descriptor: IntegrationDescriptor
	val settingsType: KClass<out IntegrationSettings>
	val fetcher: IntegrationFetcher<*>

	suspend fun start()
	suspend fun schedule(user: User, settings: IntegrationSettings)
	suspend fun setup(user: User, settings: IntegrationSettings): ConfigId

	fun routes(routing: Route) {
		// default implementation does nothing
	}
}

val Integration.name
	get() = descriptor.name

interface IntegrationFactory {
	fun create(context: IntegrationContext): Integration

	val mongoDBCodecs: Collection<Codec<*>>
	val descriptor: IntegrationDescriptor
}

abstract class DefaultIntegrationFactory(override val descriptor: IntegrationDescriptor) : IntegrationFactory {
	override val mongoDBCodecs: Collection<Codec<*>> = emptyList()
}
