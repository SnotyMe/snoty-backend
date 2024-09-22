package me.snoty.backend.scheduling.jobrunr

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import me.snoty.backend.featureflags.LogFeatureFlags
import me.snoty.backend.logging.toSLF4JLevel
import org.koin.core.annotation.Single
import org.slf4j.Marker

@Single
class JobRunrQueryFilter(private val jobRunrFeatureFlags: JobRunrFeatureFlags) : TurboFilter() {
	override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?): FilterReply = when {
		logger == null || level == null || format == null -> FilterReply.NEUTRAL
		logger.name.startsWith(LogFeatureFlags.MONGO_COMMANDS_LOGGER)
			&& level.toSLF4JLevel().toInt() < jobRunrFeatureFlags.logQueries.toInt()
			&& format.contains(JOBRUNR_COLLECTION_PREFIX) -> FilterReply.DENY
		else -> FilterReply.NEUTRAL
	}
}
