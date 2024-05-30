package me.snoty.integration.moodle

import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.integration.common.*
import me.snoty.integration.common.config.ConfigId
import me.snoty.integration.common.utils.RedactInJobName
import me.snoty.integration.moodle.calendar.iCalRoutes
import org.jobrunr.jobs.lambdas.JobRequest
import java.util.*

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	@RedactInJobName
	val appSecret: String,
	@Contextual
	override val id: ConfigId = ConfigId()
) : IntegrationSettings {
	override val instanceId = baseUrl.instanceId
}

class MoodleIntegration(
	context: IntegrationContext,
	moodleAPI: MoodleAPI = MoodleAPIImpl()
) : AbstractIntegration<MoodleSettings, MoodleJobRequest, Long>(
	DESCRIPTOR,
	MoodleSettings::class,
	MoodleFetcher.Factory(moodleAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "moodle"
		val DESCRIPTOR = IntegrationDescriptor(INTEGRATION_NAME)
	}

	override fun createRequest(config: IntegrationConfig<MoodleSettings>): JobRequest =
		MoodleJobRequest(config.user, config.settings)

	class Factory : DefaultIntegrationFactory(DESCRIPTOR) {
		override fun create(context: IntegrationContext): Integration {
			return MoodleIntegration(context)
		}
	}

	override fun routes(routing: Route) {
		routing.iCalRoutes(integrationConfigService, entityStateService)
	}
}

data class MoodleJobRequest(
	val userId: UUID,
	val settings: MoodleSettings
) : JobRequest {
	override fun getJobRequestHandler() = MoodleFetcher::class.java
}
