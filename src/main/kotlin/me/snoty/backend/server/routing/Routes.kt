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
import org.koin.ktor.ext.get as getDependency

fun Application.addResources(resources: List<Resource>) = routing {
	val logger = KotlinLogging.logger {}
	resources.forEach {
		logger.debug { "Adding resource $it" }
		with (it) { register() }
	}

	val buildInfo: BuildInfo = getDependency()
	openAPI("/openapi.json") {
		info = OpenApiInfo(title = buildInfo.application, version = buildInfo.version)
		codegen = OpenAPIGenerator().apply {

		}
	}

	swaggerUI("/swagger") {
		source = OpenApiDocSource.Routing(ContentType.Application.Json)
	}
}
