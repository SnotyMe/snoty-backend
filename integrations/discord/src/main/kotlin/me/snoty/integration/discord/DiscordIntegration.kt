package me.snoty.integration.discord

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.FieldName
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.eachWithSettings
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.core.annotation.Single

@Serializable
data class DiscordSettings(
	override val name: String = "Discord",
	@FieldName("Webhook URL")
	@FieldCensored
	val webhookUrl: String,
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
	override suspend fun NodeHandleContext.process(
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

		client.post(config.webhookUrl) {
			contentType(ContentType.Application.Json)
			setBody(data)
		}
	}
}
