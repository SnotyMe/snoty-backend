package me.snoty.backend.server.routing

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.swagger.codegen.v3.generators.openapi.OpenAPIGenerator
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

	val buildInfo: BuildInfo = getDependency()
	val extraSchemas: List<JsonSchema> = koin.getFromAllScopes()
	openAPI("/openapi.json") {
		info = OpenApiInfo(title = buildInfo.application, version = buildInfo.version)
		codegen = OpenAPIGenerator()
		components = Components(
			schemas = extraSchemas.associateBy { it.title ?: error("All extra schemas must have a title") }
		)
	}

	swaggerUI("/swagger") {
		source = OpenApiDocSource.Routing(ContentType.Application.Json)
	}
}
