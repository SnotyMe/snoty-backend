package me.snoty.integration.discord

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import me.snoty.integration.common.NodeContextBuilder
import me.snoty.integration.common.utils.RedactInJobName
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.EdgeVertices
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.*
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

@Serializable
data class DiscordSettings(
	@RedactInJobName
	val webhookUrl: String,
) : NodeSettings

class DiscordNodeHandler(private val codecRegistry: CodecRegistry) : NodeHandler {
	override val position: NodePosition = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = DiscordSettings::class

	private val client = HttpClient {
		install(ContentNegotiation) {
			json()
		}
	}

	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		val config: DiscordSettings = node.getConfig(codecRegistry)
		// TODO: replace with mapper shit
		val input = DiscordWebhook.Message("Hello World!")

		val data = when {
			input is DiscordWebhook.Message -> input
			else -> throw IllegalArgumentException("Invalid input type")
		}

		client.post(config.webhookUrl) {
			contentType(ContentType.Application.Json)
			setBody(data)
		}

		return EdgeVertices.EndOfFlow
	}
}

class DiscordNodeHandlerContributor : NodeHandlerContributor {
	override fun contributeHandlers(registry: NodeRegistry, nodeContextBuilder: NodeContextBuilder) {
		registry.registerIntegrationHandler("discord", nodeContextBuilder) { context ->
			DiscordNodeHandler(context.codecRegistry)
		}
	}
}
