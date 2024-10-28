package me.snoty.backend.server.resources.task

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.snoty.backend.config.Group
import me.snoty.backend.scheduling.AdminTasks
import me.snoty.backend.scheduling.Task
import me.snoty.backend.server.routing.Resource
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUserGroups
import me.snoty.backend.utils.requireAnyGroup
import me.snoty.backend.utils.respondStatus
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("task")
fun taskResources(adminTasks: AdminTasks) = Resource {
	authenticate("jwt-auth") {
		route("task") {
			val tasks = adminTasks.getTasks().associateBy(Task::name)

			get("list") {
				val groups = call.getUserGroups()

				val filteredTasks = tasks.values.filter {
					groups.contains(Group.ADMIN) || groups.contains(it.name)
				}

				call.respond(filteredTasks)
			}

			post("trigger") {
				val action = call.request.queryParameters["action"] ?: return@post call.respondStatus(BadRequestException("Action is missing"))
				call.requireAnyGroup(Group.ADMIN, action)

				val task = tasks[action] ?: return@post call.respondStatus(NotFoundException("Task not found"))

				withContext(Dispatchers.IO) {
					task.action()
				}

				call.respond(HttpStatusCode.OK)
			}
		}
	}
}
