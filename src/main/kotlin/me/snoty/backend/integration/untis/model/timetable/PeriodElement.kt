package me.snoty.backend.integration.untis.model.timetable

import kotlinx.serialization.Serializable

@Serializable
/**
 * KV Mapping for related entities
 *
 *
 * for ex.:
 * - type: CLASS, id: 5
 * - type: TEACHER, id: 10
 */
data class PeriodElement(
	val type: String,
	val id: Int,
	val orgId: Int = id
)
