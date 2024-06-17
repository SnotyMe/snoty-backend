package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.ConfigId

interface WorkflowHandler {
	fun <T> consume(source: ConfigId, entity: T)
}
