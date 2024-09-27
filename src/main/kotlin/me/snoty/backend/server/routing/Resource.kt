package me.snoty.backend.server.routing

import io.ktor.server.routing.*

fun interface Resource {
	fun Routing.register()
}

// shitty delegate function because Kotlin removed Extension Receivers
fun Resource.register(routing: Routing) = with(routing) {
	register()
}
