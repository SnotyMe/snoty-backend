package me.snoty.backend.database.sql

import me.snoty.backend.utils.quoted
import me.snoty.backend.utils.unquoted
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

fun NodeDescriptor.sqlTableName(suffixName: String)
	= "nodes:${name}:${suffixName}".quoted()

/**
 * [4.1. Lexical Structure - 4.1.1. Identifiers and Key Words](https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS)
 */
val PG_NAME_REGEX = "[^a-z_\\d$]".toRegex()

fun Table.pkName(vararg column: Column<*>) =
	pkName(column.joinToString("_") { it.name })

fun Table.pkName(suffix: String) =
	"pk_${tableName.unquoted().replace(PG_NAME_REGEX, "_")}_$suffix"

fun Table.SanitizedPrimaryKey(column: Column<*>, vararg columns: Column<*>) =
	PrimaryKey(column, *columns, name = pkName(column, *columns))
