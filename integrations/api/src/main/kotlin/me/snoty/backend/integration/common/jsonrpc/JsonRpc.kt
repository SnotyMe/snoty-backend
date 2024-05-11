package me.snoty.backend.integration.common.jsonrpc

import io.ktor.http.*

val ContentType.Application.JsonRpc: ContentType
	get() = ContentType("application", "json-rpc")
