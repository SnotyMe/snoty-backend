package me.snoty.backend.server.routing

import io.ktor.server.routing.*

fun interface Resource {
	context(Routing)
	fun register()
}
