package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend inline fun <reified T : Any> ApplicationCall.respondCaching(entity: T) {
	val entityEtag = entity.hashCode().toString()
	response.headers.append(HttpHeaders.ETag, entityEtag)

	if (request.headers[HttpHeaders.IfNoneMatch] == entityEtag) {
		respond(HttpStatusCode.NotModified)
	} else {
		respond(entity)
	}
}
