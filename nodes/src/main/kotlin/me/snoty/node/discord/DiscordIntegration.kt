package me.snoty.node.discord

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.schema.FieldName
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialRef
import me.snoty.backend.wiring.credential.RegisterCredential
import me.snoty.backend.wiring.credential.resolve
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.eachWithSettings
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
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
	val credentials: CredentialRef<DiscordWebhookCredential>? = null,
	@FieldName("Empty is Error")
	@FieldDescription("If enabled, no message content will result in an error")
	val emptyIsError: Boolean = true
) : NodeSettings

@RegisterNode(
	name = "discord",
	displayName = "Discord",
	icon = Icon(name = "logos-discord-icon"),
	stereotype = NodeStereotype.END,
	settingsType = DiscordSettings::class,
	inputType = DiscordWebhook.Message::class
)
@Single
class DiscordNodeHandler(
	private val client: HttpClient,
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: NodeInput,
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

		val credentials = config.credentials.resolve(node.userId)
		client.post(credentials.webhookUrl) {
			contentType(ContentType.Application.Json)
			setBody(data)
		}
	}
}
