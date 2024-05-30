package me.snoty.integration.untis

import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.JobRequest
import me.snoty.integration.common.*
import me.snoty.integration.common.config.ConfigId
import me.snoty.integration.common.utils.RedactInJobName
import me.snoty.integration.untis.calendar.iCalRoutes
import me.snoty.integration.untis.model.UntisDateTime
import java.util.*

@Serializable
data class WebUntisSettings(
	val baseUrl: String,
	val school: String,
	val username: String,
	@RedactInJobName
	val appSecret: String,
	@Contextual
	override val id: ConfigId = ConfigId()
) : IntegrationSettings {
	override val instanceId = baseUrl.instanceId
}

class WebUntisIntegration(
	context: IntegrationContext,
	untisAPI: WebUntisAPI = WebUntisAPIImpl()
) : AbstractIntegration<WebUntisSettings, WebUntisJobRequest, Int>(
	DESCRIPTOR,
	WebUntisSettings::class,
	WebUntisFetcher.Factory(untisAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "webuntis"
		val DESCRIPTOR = IntegrationDescriptor(name = INTEGRATION_NAME)

		val untisCodecModule = listOf(UntisDateTime.Companion)
	}

	override fun createRequest(config: IntegrationConfig<WebUntisSettings>): JobRequest =
		WebUntisJobRequest(config.user, config.settings)

	override fun routes(routing: Route) {
		routing.iCalRoutes(integrationConfigService, entityStateService)
	}

	class Factory : IntegrationFactory {
		override val mongoDBCodecs = untisCodecModule
		override val descriptor = DESCRIPTOR

		override fun create(context: IntegrationContext): Integration {
			return WebUntisIntegration(context)
		}
	}
}

data class WebUntisJobRequest(
	val userId: UUID,
	val settings: WebUntisSettings
) : JobRequest {
	override fun getJobRequestHandler() = WebUntisFetcher::class.java
}
