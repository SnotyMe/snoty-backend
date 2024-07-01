package me.snoty.backend.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import me.snoty.backend.errors.ServiceResult

suspend fun ApplicationCall.respondStatus(status: IHttpStatusException) {
	respond(status.code, status)
}

suspend fun ApplicationCall.respondServiceResult(result: ServiceResult) {
	respond(HttpStatusCode.fromValue(result.httpCode), result)
}
