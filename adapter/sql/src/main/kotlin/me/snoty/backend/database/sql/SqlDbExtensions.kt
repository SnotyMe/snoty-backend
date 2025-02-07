package me.snoty.backend.database.sql

import me.snoty.backend.utils.quoted
import me.snoty.backend.utils.unquoted
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

fun NodeDescriptor.sqlTableName(suffixName: String)
	= "nodes:${name}:${suffixName}".quoted()

fun Table.pkName(vararg column: Column<*>) =
	pkName(column.joinToString("_") { it.name })

fun Table.pkName(suffix: String) =
	"\"pk_${tableName.unquoted()}_$suffix\""

fun Table.SanitizedPrimaryKey(column: Column<*>, vararg columns: Column<*>) =
	PrimaryKey(column, *columns, name = pkName(column, *columns))
