package me.snoty.integration.discord

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import me.snoty.integration.common.utils.RedactInJobName
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.*
import org.slf4j.Logger
import kotlin.reflect.KClass

@Serializable
data class DiscordSettings(
	@RedactInJobName
	val webhookUrl: String,
	val emptyIsError: Boolean = true
) : NodeSettings

class DiscordNodeHandler(
	override val nodeHandlerContext: NodeHandlerContext,
	private val client: HttpClient
) : NodeHandler {
	override val position: NodePosition = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = DiscordSettings::class

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
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

class DiscordNodeHandlerContributor : NodeHandlerContributor {
	override fun contributeHandlers(registry: NodeRegistry, nodeContextBuilder: NodeContextBuilder) {
		registry.registerIntegrationHandler("discord", nodeContextBuilder) { context ->
			DiscordNodeHandler(context, context.httpClient())
		}
	}
}