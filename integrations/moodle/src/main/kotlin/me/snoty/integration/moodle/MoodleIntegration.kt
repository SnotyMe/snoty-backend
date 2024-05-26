package me.snoty.integration.moodle

import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.integration.common.*
import me.snoty.integration.common.diff.state.UserEntityStates
import me.snoty.integration.common.diff.UserEntityChanges
import me.snoty.integration.moodle.calendar.iCalRoutes
import org.jobrunr.jobs.lambdas.JobRequest
import java.util.*

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	val appSecret: String
) : IntegrationSettings {
	override val instanceId = baseUrl.instanceId
}

typealias MoodleStateCollection = MongoCollection<UserEntityStates>
typealias MoodleChangesCollection = MongoCollection<UserEntityChanges>

class MoodleIntegration(
	context: IntegrationContext,
	moodleAPI: MoodleAPI = MoodleAPIImpl()
) : AbstractIntegration<MoodleSettings, MoodleJobRequest, Long>(
	INTEGRATION_NAME,
	MoodleSettings::class,
	MoodleFetcher.Factory(moodleAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "moodle"
	}

	override fun createRequest(config: IntegrationConfig<MoodleSettings>): JobRequest =
		MoodleJobRequest(config.user, config.settings)

	class Factory : DefaultIntegrationFactory() {
		override fun create(context: IntegrationContext): Integration {
			return MoodleIntegration(context)
		}
	}

	override fun routes(routing: Route) {
		routing.iCalRoutes(stateCollection)
	}
}

data class MoodleJobRequest(
	val userId: UUID,
	val settings: MoodleSettings
) : JobRequest {
	override fun getJobRequestHandler() = MoodleFetcher::class.java
}
