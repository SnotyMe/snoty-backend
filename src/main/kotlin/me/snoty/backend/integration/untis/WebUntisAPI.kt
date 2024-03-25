package me.snoty.backend.integration.untis

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.reflect.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.snoty.backend.integration.common.jsonrpc.JsonRpcResponse
import me.snoty.backend.integration.untis.param.UntisParam
import me.snoty.backend.utils.JsonRpc
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import java.net.URI

interface WebUntisAPI {
	suspend fun <T> request(type: TypeInfo, request: UntisRequest): JsonRpcResponse<T>
}

/**
 * Bridge to allow for easier usage of the WebUntis API interface
 *
 * Interfaces cannot have reified type parameters, so this extension function
 * is a workaround to bridge this gap
 */
suspend inline fun <reified T> WebUntisAPI.request(request: UntisRequest): T = request<T>(typeInfo<JsonRpcResponse<T>>(), request).result

class WebUntisAPIImpl : WebUntisAPI {
	private val logger = LoggerFactory.getLogger(WebUntisAPIImpl::class.java)
	private val httpClient = HttpClient(Apache) {
		install(ContentNegotiation) {
			json()
			serialization(ContentType.Application.JsonRpc, Json {
				encodeDefaults = true
				isLenient = true
				allowSpecialFloatingPointValues = true
				allowStructuredMapKeys = true
				prettyPrint = false
				useArrayPolymorphism = false
				ignoreUnknownKeys = true
			})
		}
	}

	override suspend fun <T> request(type: TypeInfo, request: UntisRequest): JsonRpcResponse<T> {
		val response = httpClient.post(request.toUri().toASCIIString()) {
			header("Content-Type", "application/json; charset=UTF-8")
			setBody(request.data)
		}
		logger.debug("Fetched WebUntis Data: HTTP {} - {}", response.status.value, response.bodyAsText())

		return response.body(type)
	}
}

class UntisRequest(private val urlBuilder: URIBuilder) {
	lateinit var data: UntisPayload

	constructor(settings: WebUntisSettings, block: UntisRequest.() -> Unit = {}) : this(
		URIBuilder(settings.baseUrl)
			.setPath("/WebUntis/jsonrpc_intern.do")
			.setParameter("school", settings.school)
	) {
		block()
	}

	fun toUri(): URI = urlBuilder
		.setParameter("v", "a5.2.3") // required according to BetterUntis
		.build()
}

@Serializable
class UntisPayload() {
	var id = "-1"
	val jsonrpc = "2.0"
	var method = ""
	val params: MutableList<UntisParam> = mutableListOf()

	constructor(block: UntisPayload.() -> Unit) : this() {
		block(this)
	}

	fun param(toAdd: UntisParam) {
		params.add(toAdd)
	}
}
