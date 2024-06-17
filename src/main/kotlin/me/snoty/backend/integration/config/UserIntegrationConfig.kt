package me.snoty.backend.integration.config

import me.snoty.integration.common.IntegrationConfig
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

data class UserIntegrationConfig(
	@BsonId
	val userId: UUID,
	val sources: Map<String, IntegrationConfig<*>>,
	val targets: Map<String, IntegrationConfig<*>>
)
