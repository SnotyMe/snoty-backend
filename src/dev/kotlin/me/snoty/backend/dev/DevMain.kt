package me.snoty.backend.dev

import me.snoty.backend.dev.spi.DevManager
import me.snoty.backend.startApplication

fun main() {
	DevManager.runDevFunctions()
	startApplication(devModule)
}
