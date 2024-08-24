package me.snoty.backend

import kotlinx.coroutines.runBlocking
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.spi.DevManager
import me.snoty.integration.common.integrationApiModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

fun main() = runBlocking {
	setupLogbackFilters()
	DevManager.runDevFunctions()

	val koin = startKoin {
		modules(
			apiModule,
			defaultModule,
			integrationApiModule,
			module {
				single<Koin> { this.getKoin() }
			}
		)
	}

	val application = Application(koin.koin)
	application.start()
}
