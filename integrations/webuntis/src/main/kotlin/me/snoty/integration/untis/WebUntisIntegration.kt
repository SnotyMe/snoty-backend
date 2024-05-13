package me.snoty.integration.untis

import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.JobRequest
import me.snoty.integration.common.*
import me.snoty.integration.common.diff.EntityStateTable
import me.snoty.integration.common.diff.ID
import me.snoty.integration.untis.calendar.iCalRoutes
import org.jetbrains.exposed.sql.Column
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

object WebUntisEntityStateTable : EntityStateTable<Int>() {
	override val id: Column<Int> = integer(ID)
	override val primaryKey = buildPrimaryKey()
}

class WebUntisIntegration(
	context: IntegrationContext,
	untisAPI: WebUntisAPI = WebUntisAPIImpl()
) : AbstractIntegration<WebUntisSettings, WebUntisJobRequest>(
	INTEGRATION_NAME,
	WebUntisSettings::class,
	WebUntisEntityStateTable,
	WebUntisFetcher.Factory(untisAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "webuntis"
	}

	override fun createRequest(config: IntegrationConfig<WebUntisSettings>): JobRequest =
		WebUntisJobRequest(config.user, config.settings)

	class Factory : IntegrationFactory {
		override fun create(context: IntegrationContext): Integration {
			return WebUntisIntegration(context)
		}
	}

	override fun routes(routing: Route) {
		routing.iCalRoutes()
	}
}

data class WebUntisJobRequest(
	val userId: UUID,
	val settings: WebUntisSettings
) : JobRequest {
	override fun getJobRequestHandler() = WebUntisFetcher::class.java
}
