package me.snoty.backend.integration.config.flow

import me.snoty.backend.integration.config.ConfigId

sealed class ConnectionDescriptor(val type: String) {
	data class IntegrationConnectionDescriptor(val id: ConfigId) : ConnectionDescriptor("integration")
	data class NodeConnectionDescriptor(val id: NodeId) : ConnectionDescriptor("node")
}
