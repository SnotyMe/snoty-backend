package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.addResources(resources: List<Resource>) = routing {
	resources.forEach {
		it()
	}
}

typealias Resource = Routing.() -> Unit
fun resource(block: Resource): Resource = block
