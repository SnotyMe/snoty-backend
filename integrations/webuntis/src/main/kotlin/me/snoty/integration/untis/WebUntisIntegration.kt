package me.snoty.integration.untis

import kotlinx.serialization.Serializable
import me.snoty.integration.common.diff.EntityStateTable
import me.snoty.integration.common.diff.ID
import me.snoty.backend.scheduling.JobRequest
import me.snoty.integration.common.*
import org.jetbrains.exposed.sql.Column
import java.util.*

@Serializable
data class WebUntisSettings(
	val baseUrl: String,
	val school: String,
	val username: String,
	val appSecret: String
) : IntegrationSettings()

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

	override fun getInstanceId(config: IntegrationConfig<WebUntisSettings>) =
		config.settings.baseUrl.hashCode()

	override fun createRequest(config: IntegrationConfig<WebUntisSettings>): JobRequest =
		WebUntisJobRequest(config.user, config.settings)

	class Factory : IntegrationFactory {
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
