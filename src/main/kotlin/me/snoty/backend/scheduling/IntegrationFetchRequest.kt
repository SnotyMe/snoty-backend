package me.snoty.backend.scheduling

import org.jobrunr.jobs.lambdas.JobRequest

class IntegrationFetchRequest : JobRequest {
	override fun getJobRequestHandler(): Class<IntegrationFetchRequestHandler> {
		return IntegrationFetchRequestHandler::class.java
	}
}
