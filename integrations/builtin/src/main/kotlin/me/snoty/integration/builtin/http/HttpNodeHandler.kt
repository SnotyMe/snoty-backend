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
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
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
				setBody(it.body)
			}

			val bodyText = response.bodyAsText()

			val body = when (settings.serializeOutputAs) {
				HttpNodeSerializer.TEXT -> bodyText
				HttpNodeSerializer.JSON -> Document.parse(bodyText, codecRegistry.get(Document::class.java))
			}

			Document(
				mapOf<String, Any>(
					HttpNodeOutput::statusCode.name to response.status.value,
					HttpNodeOutput::headers.name to response.headers.flattenEntries().toMap(),
					HttpNodeOutput::body.name to body,
				)
			)
		}

		return output.map { BsonIntermediateData(it) }
	}
}
