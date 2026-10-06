package me.snoty.backend.server.routing

import io.ktor.server.routing.Route

fun interface Resource {
    fun Route.register()
}
