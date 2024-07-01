package me.snoty.backend.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.errors.ServiceResult

suspend fun <E : HttpStatusException> ApplicationCall.respondStatus(status: E) {
	respond(status.code, status as HttpStatusException)
}

suspend fun ApplicationCall.respondServiceResult(result: ServiceResult) {
	respond(HttpStatusCode.fromValue(result.httpCode), result)
}
