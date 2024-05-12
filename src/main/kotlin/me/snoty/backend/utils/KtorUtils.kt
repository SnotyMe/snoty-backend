package me.snoty.backend.utils

import io.ktor.server.application.*
import io.ktor.server.response.*
import me.snoty.backend.server.handler.IHttpStatusException

suspend fun ApplicationCall.respondStatus(status: IHttpStatusException) {
	respond(status.code, status)
}
