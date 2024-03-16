package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import me.snoty.backend.config.Config
import me.snoty.backend.server.handler.*
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.utils.getKoinInstance
import me.snoty.backend.utils.ifDev
import me.snoty.backend.utils.otherwise
import me.snoty.backend.utils.respondStatus

fun Application.configureRouting() {
	val config = getKoinInstance<Config>()
	install(StatusPages) {
		// catch manually created exceptions
		// the casts to IHttpStatusException are necessary because of a bug in kotlinx.serialization
		// that causes "no serializer found" exceptions when trying to serialize the concrete instance
		exception<HttpStatusException> { call, cause ->
			call.respondStatus(cause as IHttpStatusException)
		}
		// catch-all for exceptions
		exception<Throwable> { call, cause ->
			val message = config ifDev { cause.message ?: cause.javaClass.simpleName } otherwise { UNKNOWN_ERROR }
			call.respondStatus(InternalServerErrorException(message))
		}
		// catch-all for unhandled calls
		unhandled { call ->
			call.respondStatus(NotFoundException())
		}
	}

	install(DoubleReceive)

	configureResources()
}

private fun Application.configureResources() {
	aboutResource()
}
