package me.snoty.backend.server.resources

import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.snoty.backend.build.BuildInfo
import java.net.InetAddress

@Serializable
data class AboutResponse(
	val serverTime: Instant,
	val hostname: String,
	val buildInfo: BuildInfo
)

fun Routing.aboutResource(buildInfo: BuildInfo) {
	val hostname = InetAddress.getLocalHost().hostName
	get("/info") {
		call.respond(AboutResponse(
			serverTime = Clock.System.now(),
			hostname = hostname,
			buildInfo = buildInfo
		))
	}
}
