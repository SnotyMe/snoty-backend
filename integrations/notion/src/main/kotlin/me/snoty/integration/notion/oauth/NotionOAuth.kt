package me.snoty.integration.notion.oauth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.backend.config.Config
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.buildHandlerNodeApiUrl
import me.snoty.integration.notion.NOTION_BASE_URL
import me.snoty.integration.notion.NotionConfig
import org.koin.core.annotation.Factory

interface NotionOAuth {
	val redirectUri: String

	suspend fun exchangeToken(code: String): String
}

@Factory
class NotionOAuthImpl(
	private val httpClient: HttpClient,
	private val notionConfig: NotionConfig,
	config: Config,
	descriptor: NodeDescriptor,
) : NotionOAuth {
	override val redirectUri = buildHandlerNodeApiUrl(config, descriptor, "callback")

	@Serializable
	data class NotionTokenResponse(@SerialName("access_token") val accessToken: String)

	@Serializable
	data class TokenRequest(
		@SerialName("grant_type") val grantType: String = "authorization_code",
		val code: String,
		@SerialName("redirect_uri") val redirectUri: String,
	)

	override suspend fun exchangeToken(code: String) = httpClient.post("$NOTION_BASE_URL/oauth/token") {
		basicAuth(username = notionConfig.clientId, password = notionConfig.clientSecret)
		contentType(ContentType.Application.Json)
		setBody(TokenRequest(
			code = code,
			redirectUri = redirectUri,
		))
	}.body<NotionTokenResponse>().accessToken
}
