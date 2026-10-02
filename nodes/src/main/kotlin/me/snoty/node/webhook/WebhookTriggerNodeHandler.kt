package me.snoty.node.webhook

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.NodeScheduler
import me.snoty.backend.schema.EmptySchema
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.utils.ForbiddenException
import me.snoty.backend.utils.NodeSerializationUtils
import me.snoty.backend.utils.SerializationFormat
import me.snoty.backend.utils.respondStatus
import me.snoty.backend.wiring.data.IntermediateDataMapperRegistry
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.backend.wiring.node.routing.NodeRouteFactory
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import org.bson.Document
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Serializable
data class WebhookTriggerNodeSettings(
	@FieldDescription("Optional Secret to verify the webhook request")
	@FieldCensored
	val secret: String? = null,
	val serializeAs: SerializationFormat,
): NodeSettings

data class WebhookTriggerNodeOutput(
	val payload: EmptySchema,
)

@RegisterNode(
	name = "webhook_trigger",
	displayName = "Webhook Trigger",
	icon = Icon(name = "lucide-webhook"),
	stereotype = NodeStereotype.START,
	settingsType = WebhookTriggerNodeSettings::class,
	outputType = WebhookTriggerNodeOutput::class
)
@Single
class WebhookTriggerNodeHandler(
	nodeRouteFactory: NodeRouteFactory,
	nodeSerializationUtils: NodeSerializationUtils,
	intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	nodeScheduler: NodeScheduler,
): NodeHandler {
	init {
		nodeRouteFactory("webhook", HttpMethod.Post, verifyUser = false) { node ->
			val settings = node.getConfig<WebhookTriggerNodeSettings>()
			val secret = settings.secret
			if (!secret.isNullOrEmpty() && secret != call.queryParameters["secret"]) {
				call.respondStatus(ForbiddenException("Invalid webhook secret"))
				return@nodeRouteFactory
			}

			val payloadText = call.receiveText()
			val payload = nodeSerializationUtils.deserialize(settings.serializeAs, payloadText)

			val inputDocument = Document(WebhookTriggerNodeOutput::payload.name, payload)
			val mapper = intermediateDataMapperRegistry.getFirstCompatibleMapper(inputDocument::class)
			val input = mapper.serialize(inputDocument)

			nodeScheduler.trigger(node, logLevel = Level.INFO, listOf(input))

			call.respond(HttpStatusCode.Accepted)
		}
	}

	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: NodeInput
	) = input
}
