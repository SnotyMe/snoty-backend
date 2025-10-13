package me.snoty.backend

import kotlinx.coroutines.runBlocking
import me.snoty.apiModule
import me.snoty.backend.adapter.AdapterSelector
import me.snoty.backend.authentication.AuthenticationAdapter
import me.snoty.backend.database.DatabaseAdapter
import me.snoty.backend.events.EventHandler
import me.snoty.backend.featureflags.setupFeatureFlags
import me.snoty.backend.injection.getFromAllScopes
import me.snoty.backend.logging.setupLogbackFilters
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.logger.SLF4JLogger
import kotlin.system.exitProcess

fun main() = startApplication()

fun startApplication(vararg extraModules: Module) = runBlocking {
	val koin = startKoin {
		logger(SLF4JLogger(level = Level.DEBUG))
		modules(
			*extraModules,
			module {
				single<Koin> { this.getKoin() }
			},
			apiModule,
			defaultModule,
		)
	}.koin

	val adapterSelector: AdapterSelector = koin.get()
	adapterSelector.load(DatabaseAdapter::class, DatabaseAdapter.CONFIG_KEY)
	adapterSelector.load(AuthenticationAdapter::class, AuthenticationAdapter.CONFIG_KEY)

	// setup logging related things
	setupLogbackFilters(koin.getAll(), koin.getFromAllScopes())
	setupFeatureFlags(koin.get(), koin.getFromAllScopes())

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
