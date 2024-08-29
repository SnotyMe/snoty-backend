package me.snoty.backend

import kotlinx.coroutines.runBlocking
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.spi.DevManager
import me.snoty.integration.common.integrationApiModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.logger.SLF4JLogger

fun main() = runBlocking {
	setupLogbackFilters()
	DevManager.runDevFunctions()

	val koin = startKoin {
		logger(SLF4JLogger(level = Level.INFO))
		modules(
			module {
				single<Koin> { this.getKoin() }
			},
			apiModule,
			defaultModule,
			integrationApiModule,
		)
	}

	val application = Application(koin.koin)
	application.start()
}
