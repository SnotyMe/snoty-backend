package me.snoty.backend.server.resources

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.notifications.NotificationAttributes
import me.snoty.backend.notifications.NotificationService
import me.snoty.backend.server.routing.Resource
import me.snoty.backend.utils.getUser
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("notification")
fun notificationResource(notificationService: NotificationService) = Resource {
	authenticate("jwt-auth") {
		route("notification") {
			get("list") {
				val user = call.getUser()

				call.respond(notificationService.findByUser(user.id.toString()))
			}

			get("count") {
				val user = call.getUser()

				call.respond(notificationService.unresolvedByUser(user.id.toString()))
			}

			put("resolve") {
				val user = call.getUser()
				val attributes: NotificationAttributes = call.receive()

				notificationService.resolve(user.id.toString(), attributes)

				call.respond(HttpStatusCode.NoContent)
			}
		}
	}
}
