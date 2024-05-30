package me.snoty.backend.integration.config

import me.snoty.integration.common.IntegrationConfig
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class UserIntegrationConfig(
	@BsonId
	val userId: UUID,
	val configs: Map<String, IntegrationConfig<*>>
)
