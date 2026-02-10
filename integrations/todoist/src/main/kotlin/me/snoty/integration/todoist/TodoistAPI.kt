package me.snoty.integration.todoist

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.snoty.integration.todoist.model.TodoistTaskCreateDTO
import me.snoty.integration.todoist.model.TodoistTaskCreateResponse
import me.snoty.integration.todoist.model.TodoistTaskUpdateDTO
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Single

interface TodoistAPI {
	suspend fun createTask(taskCreateDTO: TodoistTaskCreateDTO): TodoistTaskCreateResponse
	suspend fun updateTask(id: String, taskUpdateDTO: TodoistTaskUpdateDTO): HttpResponse
	suspend fun closeTask(id: String): HttpResponse
}

private const val TODOIST_BASE_URL = "https://api.todoist.com/rest/v2"

@Single
class TodoistAPIFactory(private val client: HttpClient) {
	operator fun invoke(token: String): TodoistAPI = TodoistAPIImpl(client, token)
}

@Factory
class TodoistAPIImpl(val client: HttpClient, @InjectedParam private val token: String) : TodoistAPI {
	override suspend fun createTask(taskCreateDTO: TodoistTaskCreateDTO): TodoistTaskCreateResponse =
		client.post("$TODOIST_BASE_URL/tasks") {
			header("Authorization", "Bearer $token")
			contentType(ContentType.Application.Json)
			accept(ContentType.Application.Json)
			setBody(taskCreateDTO)
		}.body()

	override suspend fun updateTask(id: String, taskUpdateDTO: TodoistTaskUpdateDTO) =
		client.post("$TODOIST_BASE_URL/tasks/$id") {
			header("Authorization", "Bearer $token")
			contentType(ContentType.Application.Json)
			setBody(taskUpdateDTO)
		}

	override suspend fun closeTask(id: String) = runCatching {
		client.post("$TODOIST_BASE_URL/tasks/$id/close") {
			header("Authorization", "Bearer $token")
		}
	}.getOrElse { exception ->
		// most likely a deleted task => closing is obsolete, consider it done
		if (exception is ClientRequestException && exception.response.status == HttpStatusCode.NotFound) {
			exception.response
		}

		throw exception
	}
}
