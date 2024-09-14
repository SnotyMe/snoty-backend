package me.snoty.backend

import kotlinx.coroutines.runBlocking
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.integration.common.integrationApiModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.logger.SLF4JLogger

fun main() = startApplication()

fun startApplication(vararg extraModules: Module) = runBlocking {
	val koin = startKoin {
		logger(SLF4JLogger(level = Level.INFO))
		modules(
			*extraModules,
			module {
				single<Koin> { this.getKoin() }
			},
			apiModule,
			defaultModule,
			integrationApiModule,
		)
	}.koin
	setupLogbackFilters(koin.getAll(), koin.getAll())

	val application = Application(koin)
	application.start()
}
