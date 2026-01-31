package me.snoty.backend.database.sql.utils

import me.snoty.backend.utils.randomV7
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import kotlin.uuid.Uuid

/**
 * Similar to [UuidTable][org.jetbrains.exposed.v1.core.dao.id.UuidTable], but uses UUIDv7 as the default value for the id column.
 */
open class UuidTable(name: String = "", columnName: String = "id") : IdTable<Uuid>(name) {
	final override val id = uuid(columnName).clientDefault {
		Uuid.randomV7()
	}.entityId()

	final override val primaryKey = PrimaryKey(id)
}
