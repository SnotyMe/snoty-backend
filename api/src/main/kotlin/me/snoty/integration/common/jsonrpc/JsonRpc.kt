package me.snoty.integration.common.jsonrpc

import io.ktor.client.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.util.*
import io.opentelemetry.api.common.AttributesBuilder
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import io.opentelemetry.semconv.incubating.RpcIncubatingAttributes

val ContentType.Application.JsonRpc: ContentType
	get() = ContentType("application", "json-rpc")

interface JsonRpcRequest {
	val version: String
	val method: String
}

fun AttributesBuilder.setRPCAttributes(request: JsonRpcRequest) {
	put(RpcIncubatingAttributes.RPC_SYSTEM, "jsonrpc")
	put(RpcIncubatingAttributes.RPC_JSONRPC_VERSION, request.version)
	put(RpcIncubatingAttributes.RPC_METHOD, request.method)
}

val RPC_REQUEST_ATTRIBUTE = AttributeKey<JsonRpcRequest>("rpcRequest")

fun HttpClient.rpcConfig(block: HttpClientConfig<*>.() -> Unit): HttpClient = config {
	block()
	install(KtorClientTelemetry) {
		attributesExtractor {
			onStart {
				val request = request.attributes[RPC_REQUEST_ATTRIBUTE]
				attributes.setRPCAttributes(request)
			}
		}
	}
}

fun HttpRequestBuilder.setRpcBody(body: JsonRpcRequest) {
	attributes.put(RPC_REQUEST_ATTRIBUTE, body)
	setBody(body)
}
