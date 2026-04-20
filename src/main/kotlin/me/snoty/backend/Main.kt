package me.snoty.backend

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.filter.Filter
import kotlinx.coroutines.runBlocking
import me.snoty.backend.adapter.AdapterSelector
import me.snoty.backend.authentication.AuthenticationAdapter
import me.snoty.backend.database.DatabaseAdapter
import me.snoty.backend.events.EventHandler
import me.snoty.backend.featureflags.FeatureFlagsAdapter
import me.snoty.backend.featureflags.LogFeatureFlagsContainer
import me.snoty.backend.featureflags.setupFeatureFlags
import me.snoty.backend.injection.getFromAllScopes
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.wiring.flow.execution.ExecutionEventAdapter
import org.koin.core.Koin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger
import org.koin.plugin.module.dsl.startKoin
import kotlin.system.exitProcess

fun main() = startApplication()

fun startApplication(vararg extraModules: Module) = runBlocking {
	val koin = startKoin<Application> {
		logger(SLF4JLogger(level = Level.DEBUG))
		modules(
			*extraModules,
			module {
				single<Koin> { this.getKoin() }
			},
		)
	}.koin

	val adapterSelector: AdapterSelector = koin.get()

	// base setup, needed for other systems to work properly, so we load them first
	adapterSelector.load(FeatureFlagsAdapter::class, FeatureFlagsAdapter.CONFIG_GROUP)
	val initialFilters = koin.getFromAllScopes<TurboFilter>() + koin.getFromAllScopes<Filter<ILoggingEvent>>()
	setupLogbackFilters(initialFilters)
	val initialFeatureFlags = koin.getFromAllScopes<LogFeatureFlagsContainer>()
	setupFeatureFlags(koin.get(), initialFeatureFlags)

	adapterSelector.load(DatabaseAdapter::class, DatabaseAdapter.CONFIG_GROUP)
	adapterSelector.load(AuthenticationAdapter::class, AuthenticationAdapter.CONFIG_GROUP)
	adapterSelector.load(ExecutionEventAdapter::class, ExecutionEventAdapter.CONFIG_GROUP)

	val postAdapterFilters = koin.getFromAllScopes<TurboFilter>() + koin.getFromAllScopes<Filter<ILoggingEvent>>()
	setupLogbackFilters(postAdapterFilters.filter { it !in initialFilters })
	val postAdapterFeatureFlags = koin.getFromAllScopes<LogFeatureFlagsContainer>()
	setupFeatureFlags(koin.get(), postAdapterFeatureFlags.filter { it !in initialFeatureFlags })

	koin.getAll<EventHandler>()
		// allows to register hooks that need to be executed in the application lifecycle
		.forEach { it.handleInitializationEvent(koin.get()) }

	val application: Application = koin.get()
	try {
		application.start()
	} catch (e: Throwable) {
		application.logger.error(e) { "Application failed to start" }
		exitProcess(-1)
	}
}
