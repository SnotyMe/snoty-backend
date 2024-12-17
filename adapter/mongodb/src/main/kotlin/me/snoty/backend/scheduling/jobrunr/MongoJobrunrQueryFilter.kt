package me.snoty.backend.scheduling.jobrunr

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import me.snoty.backend.database.mongo.MongoLogFeatureFlags
import me.snoty.backend.logging.toSLF4JLevel
import org.koin.core.annotation.Single
import org.slf4j.Marker

@Single
class MongoJobrunrQueryFilter(private val mongoLogFeatureFlags: MongoLogFeatureFlags) : TurboFilter() {
	override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?): FilterReply = when {
		logger == null || level == null || format == null -> FilterReply.NEUTRAL
		logger.name.startsWith(MongoLogFeatureFlags.MONGO_COMMANDS_LOGGER)
			&& level.toSLF4JLevel().toInt() < mongoLogFeatureFlags.getValue(mongoLogFeatureFlags.jobRunr).toInt()
			&& format.contains(JOBRUNR_COLLECTION_PREFIX) -> FilterReply.DENY

		else -> FilterReply.NEUTRAL
	}
}
