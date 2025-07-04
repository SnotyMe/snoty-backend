package me.snoty.integration.builtin.calendar

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CalendarEvent(
	/**
	 * ID gotten from the external system
	 */
	val id: String,
	val name: String,
	val date: Instant?,
	val startDate: Instant?,
	val endDate: Instant?,
	val description: String?,
	val location: String?,
)
