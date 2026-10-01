package me.snoty.node.http

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import me.snoty.backend.utils.NodeSerializationUtils
import me.snoty.backend.utils.proxy.withOptionalProxy
import me.snoty.backend.wiring.credential.resolveOrNull
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.getOrNull
import me.snoty.backend.wiring.data.iterableStructOutput
import me.snoty.backend.wiring.node.Icon
import me.snoty.backend.wiring.node.NodeHandleContext
import me.snoty.backend.wiring.node.NodeHandler
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import org.koin.core.annotation.Single

@RegisterNode(
	name = "http",
	displayName = "HTTP",
	icon = Icon(name = "material-symbols-http-rounded"),
	stereotype = NodeStereotype.START,
	settingsType = HttpNodeSettings::class,
	inputType = HttpNodeInput::class,
	outputType = HttpNodeOutput::class,
)
@Single
class HttpNodeHandler(
	private val httpClient: HttpClient,
	private val serializationUtils: NodeSerializationUtils,
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput {
		val settings = node.getConfig<HttpNodeSettings>()
		val proxy = settings.proxy.resolveOrNull(node.userId)
		val requests = input.mapNotNull { it.getOrNull<HttpNodeInput>() } + settings.requests

		val output = requests.map { request ->
			val response = httpClient
				.withOptionalProxy(proxy)
				.applyConfig(request)
				.request {
					url(request.url)
					method = request.method.ktor
					request.headers.forEach { (key, value) ->
						header(key, value)
					}
					setBody(request.body)
				}

			val bodyText = response.bodyAsText()
			val body = serializationUtils.deserialize(settings.serializeOutputAs, bodyText)

			HttpNodeOutput(
				request.url,
				response.status.value,
				response.headers.flattenEntries().toMap(),
				body,
			).toDocument()
		}

		return iterableStructOutput(output)
	}
}
