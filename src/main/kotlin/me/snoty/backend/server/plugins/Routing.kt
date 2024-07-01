package me.snoty.backend.server.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import me.snoty.backend.config.Config
import me.snoty.backend.utils.*

fun Application.configureRouting(config: Config) {
	val logger = KotlinLogging.logger {}
	install(StatusPages) {
		// catch manually created exceptions
		// the casts to HttpStatusException are necessary because of a bug in kotlinx.serialization
		// that causes "no serializer found" exceptions when trying to serialize the concrete instance
		exception<HttpStatusException> { call, cause ->
			logger.info { "Returning HTTP ${cause.code} with message ${cause.message}" }
			@Suppress("USELESS_CAST")
			call.respondStatus(cause as HttpStatusException)
		}
		// catch-all for exceptions
		exception<Throwable> { call, cause ->
			val message = (config
				ifDev { cause.message ?: cause.javaClass.simpleName }
				otherwise { HttpStatusCode.InternalServerError.description }
			)
			logger.warn(cause) { "Encountered uncaught exception" }
			call.respondStatus(InternalServerErrorException(message))
		}
		// catch-all for unhandled calls
		unhandled { call ->
			call.respondStatus(NotFoundException())
		}
	}

	install(DoubleReceive)
}
