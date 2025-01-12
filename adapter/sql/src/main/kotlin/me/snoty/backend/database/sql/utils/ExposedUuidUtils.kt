package me.snoty.backend.database.sql.utils

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.MariaDBDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.sql.ResultSet
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class UuidColumnType : ColumnType<Uuid>() {
	override fun sqlType(): String = currentDialect.dataTypeProvider.uuidType()

	override fun valueFromDB(value: Any): Uuid = when {
		value is Uuid -> value
		value is UUID -> value.toKotlinUuid()
		value is ByteArray -> Uuid.fromByteArray(value)
		value is String && value.matches(uuidRegexp) -> Uuid.parse(value)
		value is String -> Uuid.fromByteArray(value.toByteArray())
		else -> error("Unexpected value of type Uuid: $value of ${value::class.qualifiedName}")
	}

	override fun notNullValueToDB(value: Uuid): Any = currentDialect.dataTypeProvider.uuidToDB(value.toJavaUuid())

	override fun nonNullValueToString(value: Uuid): String = "'$value'"

	override fun readObject(rs: ResultSet, index: Int): Any? = when (currentDialect) {
		is MariaDBDialect -> rs.getBytes(index)
		else -> super.readObject(rs, index)
	}

	companion object {
		private val uuidRegexp =
			Regex("[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}", RegexOption.IGNORE_CASE)
	}
}

@OptIn(ExperimentalUuidApi::class)
fun Table.kotlinUuid(name: String): Column<Uuid> = registerColumn(name, UuidColumnType())

open class UuidTable(name: String = "", columnName: String = "id") : IdTable<Uuid>(name) {
	/** The identity column of this [UuidTable], for storing UUIDs wrapped as [EntityID] instances. */
	final override val id = kotlinUuid(columnName).clientDefault {
		// TODO: evaluate using Uuid v7
		Uuid.random()
	}.entityId()

	final override val primaryKey = PrimaryKey(id)
}
