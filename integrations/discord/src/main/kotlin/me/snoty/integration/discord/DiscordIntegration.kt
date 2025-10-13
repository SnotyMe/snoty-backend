package me.snoty.integration.discord

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialRef
import me.snoty.backend.wiring.credential.RegisterCredential
import me.snoty.backend.wiring.credential.resolve
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.FieldName
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.eachWithSettings
import me.snoty.integration.common.wiring.logger
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.core.annotation.Single

@Serializable
@RegisterCredential("DiscordWebhook")
data class DiscordWebhookCredential(
	@FieldName("Webhook URL")
	@FieldCensored
	val webhookUrl: String,
) : Credential()

@Serializable
data class DiscordSettings(
	override val name: String = "Discord",
	val credentials: CredentialRef<DiscordWebhookCredential> = CredentialRef(),
	@FieldName("Empty is Error")
	@FieldDescription("If enabled, no message content will result in an error")
	val emptyIsError: Boolean = true
) : NodeSettings

@RegisterNode(
	name = "discord",
	displayName = "Discord",
	position = NodePosition.END,
	settingsType = DiscordSettings::class,
	inputType = DiscordWebhook.Message::class
)
@Single
class DiscordNodeHandler(
	private val client: HttpClient,
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: Node,
		input: Collection<IntermediateData>,
	) = eachWithSettings<DiscordWebhook.Message, DiscordSettings>(input, node) { data, config ->
		if (data.content.isNullOrEmpty() && data.embeds.isEmpty()) {
			if (config.emptyIsError) {
				throw IllegalStateException("Discord message content and fields are empty")
			} else {
				logger.warn("Discord message content is empty, aborting...")
				return@eachWithSettings
			}
		}

		logger.info("Sending message {} to Discord webhook...", data)

		val credentials = config.credentials.resolve(node.userId.toString())
		client.post(credentials.webhookUrl) {
			contentType(ContentType.Application.Json)
			setBody(data)
		}
	}
}
