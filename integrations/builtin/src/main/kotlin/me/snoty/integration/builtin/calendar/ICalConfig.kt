package me.snoty.integration.builtin.calendar

import me.snoty.backend.config.Config
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.annotation.Single

data class ICalConfig(
	/**
	 * Domain of the calendar, used in the [UID](https://icalendar.org/New-Properties-for-iCalendar-RFC-7986/5-3-uid-property.html)
	 */
	val domain: String? = null,
)

@Single
fun provideICalConfig(configLoader: ConfigLoader, appConfig: Config): ICalConfig {
	val config = runCatching { configLoader.load<ICalConfig>("ical") }
		.getOrElse { ICalConfig() }

	return when (config.domain) {
		null -> config.copy(domain = appConfig.publicHost)
		else -> config
	}
}
