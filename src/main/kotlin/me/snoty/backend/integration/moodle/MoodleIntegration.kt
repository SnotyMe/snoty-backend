package me.snoty.backend.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.common.diff.EntityStateTable
import me.snoty.backend.integration.common.diff.ID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	val appSecret: String
)

object MoodleEntityStateTable : EntityStateTable<Long>() {
	override val id: Column<Long> = long(ID)
	override val primaryKey = buildPrimaryKey()

	init {
		SchemaUtils.createMissingTablesAndColumns(this)
	}
}
