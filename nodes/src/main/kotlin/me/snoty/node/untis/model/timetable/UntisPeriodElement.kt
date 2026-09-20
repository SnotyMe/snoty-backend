package me.snoty.node.untis.model.timetable

import kotlinx.serialization.Serializable

/**
 * KV Mapping for related entities
 *
 *
 * for ex.:
 * - type: CLASS, id: 5
 * - type: TEACHER, id: 10
 */
@Serializable
data class UntisPeriodElement(
	val type: String,
	val id: Int,
	val orgId: Int = id
)
