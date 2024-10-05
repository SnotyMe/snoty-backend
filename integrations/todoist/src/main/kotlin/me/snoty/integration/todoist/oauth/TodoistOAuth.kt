package me.snoty.integration.todoist.oauth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.integration.todoist.TodoistConfig
import org.koin.core.annotation.Single

interface TodoistOAuth {
	suspend fun exchangeToken(code: String): String
}

@Single
class TodoistOAuthImpl(private val httpClient: HttpClient, private val todoistConfig: TodoistConfig) : TodoistOAuth {
	@Serializable
	data class TodoistTokenResponse(@SerialName("access_token") val accessToken: String)

	override suspend fun exchangeToken(code: String) = httpClient.post("https://todoist.com/oauth/access_token") {
		parameter("client_id", todoistConfig.clientId)
		parameter("client_secret", todoistConfig.clientSecret)
		parameter("code", code)
	}.body<TodoistTokenResponse>().accessToken
}
