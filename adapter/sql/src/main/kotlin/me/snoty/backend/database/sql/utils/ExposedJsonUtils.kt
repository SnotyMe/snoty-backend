package me.snoty.backend.database.sql.utils

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.H2Dialect
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.postgresql.util.PGobject


class RawJsonBColumnType<@Suppress("unused") T : Any> : ColumnType<String>() {
	override fun sqlType(): String = when (currentDialect) {
		is H2Dialect -> (currentDialect as H2Dialect).originalDataTypeProvider.jsonBType()
		else -> currentDialect.dataTypeProvider.jsonBType()
	}

	override fun valueFromDB(value: Any): String? = when {
		currentDialect is PostgreSQLDialect && value is PGobject -> value.value!!
		value is String -> value
		value is ByteArray -> value.decodeToString()
		else -> error("Unexpected value $value of type ${value::class.qualifiedName}")
	}

	override fun valueToDB(value: String?): Any? {
		if (value == null) return null
		return when (currentDialect) {
			is PostgreSQLDialect -> PGobject().apply {
				type = "jsonb"
				this.value = value
			}
			else -> value
		}
	}
}

fun <T : Any> Table.rawJsonb(
	name: String,
): Column<String> =
	registerColumn(name, RawJsonBColumnType<T>())
