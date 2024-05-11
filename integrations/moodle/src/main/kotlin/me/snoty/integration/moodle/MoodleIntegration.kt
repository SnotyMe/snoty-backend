package me.snoty.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.common.*
import me.snoty.backend.integration.common.diff.EntityStateTable
import me.snoty.backend.integration.common.diff.ID
import org.jetbrains.exposed.sql.Column
import org.jobrunr.jobs.lambdas.JobRequest
import java.util.*

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	val appSecret: String
) : IntegrationSettings()

object MoodleEntityStateTable : EntityStateTable<Long>() {
	override val id: Column<Long> = long(ID)
	override val primaryKey = buildPrimaryKey()
}

class MoodleIntegration(
	context: IntegrationContext,
	moodleAPI: MoodleAPI = MoodleAPIImpl()
) : AbstractIntegration<MoodleSettings, MoodleJobRequest>(
	INTEGRATION_NAME,
	MoodleSettings::class,
	MoodleEntityStateTable,
	MoodleFetcher.Factory(moodleAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "moodle"
	}

	override fun getInstanceId(config: IntegrationConfig<MoodleSettings>): Any {
		return config.settings.baseUrl.hashCode()
	}

	override fun createRequest(config: IntegrationConfig<MoodleSettings>): JobRequest {
		return MoodleJobRequest(config.user, config.settings)
	}

	class Factory : IntegrationFactory {
		override fun create(context: IntegrationContext): Integration {
			return MoodleIntegration(context)
		}
	}
}

data class MoodleJobRequest(
	val userId: UUID,
	val settings: MoodleSettings
) : JobRequest {
	override fun getJobRequestHandler() = MoodleFetcher::class.java
}
