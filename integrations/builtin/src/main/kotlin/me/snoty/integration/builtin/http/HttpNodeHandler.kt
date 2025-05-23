package me.snoty.integration.builtin.http

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@RegisterNode(
	name = "http",
	displayName = "HTTP",
	position = NodePosition.START,
	settingsType = HttpNodeSettings::class,
	inputType = HttpNodeInput::class,
	outputType = HttpNodeOutput::class,
)
@Single
class HttpNodeHandler(
	private val httpClient: HttpClient,
	private val codecRegistry: CodecRegistry,
) : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>,
	): NodeOutput {
		val settings = node.getConfig<HttpNodeSettings>()
		val requests = input.mapNotNull { getOrNull<HttpNodeInput>(it) } + settings.requests

		val output = requests.map {
			val response = httpClient.applyConfig(it).request {
				url(it.url)
				method = it.method.ktor
				it.headers.forEach { (key, value) ->
					header(key, value)
				}
				setBody(it.body)
			}

			val bodyText = response.bodyAsText()

			val body = when (settings.serializeOutputAs) {
				HttpNodeSerializer.TEXT -> bodyText
				HttpNodeSerializer.JSON -> Document.parse(bodyText, codecRegistry.get(Document::class.java))
			}


			HttpNodeOutput(
				it.url,
				response.status.value,
				response.headers.flattenEntries().toMap(),
				body,
			).toDocument()
		}

		return iterableStructOutput(output)
	}
}
