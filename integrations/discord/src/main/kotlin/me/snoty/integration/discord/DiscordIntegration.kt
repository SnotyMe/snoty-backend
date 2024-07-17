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
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.*
import org.slf4j.Logger
import kotlin.reflect.KClass

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
	displayName = "Discord",
	type = "discord",
	position = NodePosition.END,
	settingsType = DiscordSettings::class,
	inputType = DiscordWebhook.Message::class
)
class DiscordNodeHandler(
	override val nodeHandlerContext: NodeHandlerContext,
	private val client: HttpClient = nodeHandlerContext.httpClient()
) : NodeHandler {
	override val settingsClass: KClass<out NodeSettings> = DiscordSettings::class

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val config: DiscordSettings = node.getConfig()
		val data: DiscordWebhook.Message = input.get()

		if (data.content.isNullOrEmpty() && data.embeds.isEmpty()) {
			if (config.emptyIsError) {
				throw IllegalStateException("Discord message content and fields are empty")
			} else {
				logger.warn("Discord message content is empty, aborting...")
				return
			}
		}

		logger.info("Sending message {} to Discord webhook...", data)

		client.post(config.webhookUrl) {
			contentType(ContentType.Application.Json)
			setBody(data)
		}
	}
}
