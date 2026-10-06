package me.snoty.backend.server.routing

import io.ktor.server.routing.*

// shitty delegate function because Kotlin removed Extension Receivers
fun Resource.register(routing: Route) = with(routing) {
	register()
}
