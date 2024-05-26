package me.snoty.integration.untis

import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.JobRequest
import me.snoty.integration.common.*
import me.snoty.integration.common.diff.UserEntityChanges
import me.snoty.integration.common.diff.state.UserEntityStates
import me.snoty.integration.untis.calendar.iCalRoutes
import me.snoty.integration.untis.model.UntisDateTime
import java.util.*

@Serializable
data class WebUntisSettings(
	val baseUrl: String,
	val school: String,
	val username: String,
	val appSecret: String
) : IntegrationSettings {
	override val instanceId = baseUrl.instanceId
}

typealias WebUntisStateCollection = MongoCollection<UserEntityStates>
typealias WebUntisChangesCollection = MongoCollection<UserEntityChanges>

class WebUntisIntegration(
	context: IntegrationContext,
	untisAPI: WebUntisAPI = WebUntisAPIImpl()
) : AbstractIntegration<WebUntisSettings, WebUntisJobRequest, Int>(
	INTEGRATION_NAME,
	WebUntisSettings::class,
	WebUntisFetcher.Factory(untisAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "webuntis"

		val untisCodecModule = listOf(UntisDateTime.Companion)
	}

	override fun createRequest(config: IntegrationConfig<WebUntisSettings>): JobRequest =
		WebUntisJobRequest(config.user, config.settings)

	override fun routes(routing: Route) {
		routing.iCalRoutes(stateCollection)
	}

	class Factory : IntegrationFactory {
		override val mongoDBCodecs = untisCodecModule

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
