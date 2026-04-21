package me.snoty.backend.server.routing

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.json.Json
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.injection.getFromAllScopes
import org.koin.core.Koin
import org.koin.ktor.ext.get as getDependency

fun Application.addResources(koin: Koin, resources: List<Resource>) = routing {
	val logger = KotlinLogging.logger {}
	resources.forEach {
		logger.debug { "Adding resource $it" }
		with (it) { register() }
	}

	setupOpenApi(koin)
}

fun Application.setupOpenApi(koin: Koin) = routing {
	val buildInfo: BuildInfo = getDependency()
	val extraSchemas: List<JsonSchema> = koin.getFromAllScopes()

	val info = OpenApiInfo(title = buildInfo.application, version = buildInfo.version)
	val components = Components(
		schemas = extraSchemas.associateBy { it.title ?: error("All extra schemas must have a title") }
	)

	val openApiJson = Json {
		prettyPrint = true
		encodeDefaults = false
	}

	get("/openapi.json") {
		val doc = OpenApiDoc(info = info, components = components) + application.routingRoot.descendants()
		call.respondText(openApiJson.encodeToString(doc), contentType = ContentType.Application.Json)
	}

	swaggerUI(path = "/swagger") {
		this.info = info
		this.components = components
		this.source = OpenApiDocSource.Routing(ContentType.Application.Json)
	}
}
