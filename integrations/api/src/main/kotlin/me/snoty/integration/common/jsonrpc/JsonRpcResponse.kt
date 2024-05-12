package me.snoty.integration.common.jsonrpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonRpcResponse<T>(
	@SerialName("jsonrpc")
	val jsonRpc: String,
	val id: String,
	val result: T
)
