package me.snoty.integration.common

import io.ktor.server.routing.*
import me.snoty.backend.User
import org.bson.codecs.Codec
import kotlin.reflect.KClass


interface Integration {
	val descriptor: IntegrationDescriptor
	val settingsType: KClass<out IntegrationSettings>
	val fetcher: IntegrationFetcher<*>

	fun start()
	fun schedule(user: User, settings: IntegrationSettings)
	fun setup(user: User, settings: IntegrationSettings): Long

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
