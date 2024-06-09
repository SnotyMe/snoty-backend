package me.snoty.integration.common

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.snoty.integration.common.config.ConfigId

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
interface IntegrationSettings {
	@get:JsonIgnore
	val instanceId: InstanceId
	val id: ConfigId
}

/**
 * Base integration settings type, can be used when only `instanceId` is required
 */
data class BaseIntegrationSettings(
	override val instanceId: InstanceId,
	override val id: ConfigId
) : IntegrationSettings
