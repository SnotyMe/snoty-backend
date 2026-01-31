package me.snoty.backend.server.resources

import io.ktor.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.Serializable
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.server.routing.Resource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.net.InetAddress
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class AboutResponse(
	val serverTime: Instant,
	val hostname: String,
	val buildInfo: BuildInfo
)

@Single
@Named("about")
fun AboutResource(buildInfo: BuildInfo) = Resource {
	val hostname = InetAddress.getLocalHost().hostName
	get("/info") {
		call.respond(AboutResponse(
			serverTime = Clock.System.now(),
			hostname = hostname,
			buildInfo = buildInfo
		))
	}

	get("/openapi.json") {
		val doc = OpenApiDoc(info = OpenApiInfo(buildInfo.application, buildInfo.version)) + application.routingRoot.descendants()
		call.respond(doc)
	}
}
