package me.snoty.integration.common.utils

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.SnotyJob
import me.snoty.integration.common.IntegrationConfig
import me.snoty.integration.common.IntegrationDescriptor

fun createFetcherJob(integrationDescriptor: IntegrationDescriptor, config: IntegrationConfig<*>, request: JobRequest): SnotyJob {
	val user = config.user
	val settingsString = config.settings.formatProperties()
	val integrationName = integrationDescriptor.name
	return SnotyJob(
		name = "[fetcher] <$integrationName> user=$user settings=($settingsString)",
		request
	)
}
