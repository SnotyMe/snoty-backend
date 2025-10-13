package me.snoty.backend.server.resources.task

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.snoty.backend.authentication.Role
import me.snoty.backend.scheduling.AdminTasks
import me.snoty.backend.scheduling.Task
import me.snoty.backend.server.routing.Resource
import me.snoty.backend.utils.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("task")
fun taskResources(adminTasks: AdminTasks) = Resource {
	authenticate("jwt-auth") {
		route("task") {
			val tasks = adminTasks.getTasks().associateBy(Task::name)

			get("list") {
				val roles = call.getUserRoles()

				val filteredTasks = tasks.values.filter {
					roles.contains(Role.ADMIN) || roles.contains(Role(it.name))
				}

				call.respond(filteredTasks)
			}

			post("trigger") {
				val action = call.request.queryParameters["action"] ?: return@post call.respondStatus(BadRequestException("Action is missing"))
				call.requireAnyRole(Role.ADMIN, Role(action))

				val task = tasks[action] ?: return@post call.respondStatus(NotFoundException("Task not found"))

				withContext(Dispatchers.IO) {
					task.action()
				}

				call.respond(HttpStatusCode.OK)
			}
		}
	}
}
