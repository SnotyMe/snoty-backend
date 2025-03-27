package me.snoty.integration.builtin.calendar

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
