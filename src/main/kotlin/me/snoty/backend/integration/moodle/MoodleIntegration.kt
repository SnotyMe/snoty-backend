package me.snoty.backend.integration.moodle

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.integration.common.diff.EntityStateTable
import me.snoty.backend.integration.common.diff.ID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	val appSecret: String
)

object MoodleEntityStateTable : EntityStateTable<Long>() {
	override val id: Column<Long> = long(ID)
	override val primaryKey = buildPrimaryKey()

}

lateinit var moodleDiffMetrics: EntityDiffMetrics
fun startMoodleIntegration(database: Database, metricsRegistry: MeterRegistry, metricsPool: ScheduledExecutorService) {
	transaction(database) {
		SchemaUtils.create(MoodleEntityStateTable)
	}
	moodleDiffMetrics = EntityDiffMetrics(metricsRegistry, database, "moodle", MoodleEntityStateTable)
	metricsPool.scheduleAtFixedRate(moodleDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
}
