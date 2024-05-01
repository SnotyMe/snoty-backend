package me.snoty.backend.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.common.AbstractIntegration
import me.snoty.backend.integration.common.IntegrationContext
import me.snoty.backend.integration.common.IntegrationSettings
import me.snoty.backend.integration.common.diff.EntityStateTable
import me.snoty.backend.integration.common.diff.ID
import org.jetbrains.exposed.sql.Column

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
) : AbstractIntegration<MoodleSettings>(
	INTEGRATION_NAME,
	MoodleSettings::class,
	MoodleEntityStateTable,
	MoodleScheduler.Factory(moodleAPI),
	context
) {
	companion object {
		const val INTEGRATION_NAME = "moodle"
	}
}
