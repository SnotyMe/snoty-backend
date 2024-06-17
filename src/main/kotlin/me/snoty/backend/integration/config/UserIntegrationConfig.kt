package me.snoty.backend.integration.config

import me.snoty.backend.integration.config.flow.node.ProcessorFlowNode
import me.snoty.integration.common.IntegrationConfig
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class UserIntegrationConfig(
	@BsonId
	val userId: UUID,
	val sources: Map<String, IntegrationConfig<*>>,
	val nodes: List<ProcessorFlowNode>,
	val targets: Map<String, IntegrationConfig<*>>
)
