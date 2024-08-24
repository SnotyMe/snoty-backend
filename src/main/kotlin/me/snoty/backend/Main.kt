package me.snoty.backend

import kotlinx.coroutines.runBlocking
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.spi.DevManager
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

fun main() = runBlocking {
	setupLogbackFilters()
	DevManager.runDevFunctions()

	startKoin {
		defaultModule()
	}

	val application = Application()
	application.start()
}
