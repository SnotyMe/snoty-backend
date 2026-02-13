package me.snoty.integration.notion

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.snoty.integration.notion.page.NotionPageCreateDTO
import me.snoty.integration.notion.page.NotionPageCreateResponse
import me.snoty.integration.notion.page.NotionPageUpdateDTO
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Single

const val NOTION_BASE_URL = "https://api.notion.com/v1"
const val NotionVersion = "Notion-Version"

interface NotionAPI {
	suspend fun createPage(pageCreateDTO: NotionPageCreateDTO): NotionPageCreateResponse
	suspend fun updatePage(id: String, pageUpdateDTO: NotionPageUpdateDTO): HttpResponse
	suspend fun deletePage(id: String): HttpResponse
}

@Single
class NotionAPIFactory(private val client: HttpClient) {
	operator fun invoke(token: String): NotionAPI = NotionAPIImpl(client, token)
}

@Factory
class NotionAPIImpl(
	private val client: HttpClient,
	@InjectedParam private val token: String,
) : NotionAPI {
	override suspend fun createPage(pageCreateDTO: NotionPageCreateDTO): NotionPageCreateResponse =
		client.post("$NOTION_BASE_URL/pages") {
			bearerAuth(token)
			header(NotionVersion, "2022-06-28")
			contentType(ContentType.Application.Json)
			accept(ContentType.Application.Json)
			setBody(pageCreateDTO)
		}.body()

	override suspend fun updatePage(id: String, pageUpdateDTO: NotionPageUpdateDTO): HttpResponse =
		client.patch("$NOTION_BASE_URL/pages/$id") {
			bearerAuth(token)
			header(NotionVersion, "2022-06-28")
			contentType(ContentType.Application.Json)
			accept(ContentType.Application.Json)
			setBody(pageUpdateDTO)
		}

	override suspend fun deletePage(id: String): HttpResponse =
		client.patch("$NOTION_BASE_URL/pages/$id") {
			bearerAuth(token)
			header(NotionVersion, "2022-06-28")
			contentType(ContentType.Application.Json)
			accept(ContentType.Application.Json)
			setBody("{\"archived\": true}")
		}
}
