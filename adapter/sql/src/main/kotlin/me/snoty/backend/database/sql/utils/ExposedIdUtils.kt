package me.snoty.backend.database.sql.utils

import me.snoty.core.UserId
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.TextColumnType

class UserIdColumnType : ColumnType<UserId>() {
    private val delegate = TextColumnType()

    override fun sqlType() = delegate.sqlType()

    override fun valueFromDB(value: Any): UserId =
        UserId(delegate.valueFromDB(value))

    override fun valueToDB(value: UserId?): Any? =
        delegate.valueToDB(value?.value)
}

fun Table.userId(name: String): Column<UserId> =
    registerColumn(name, UserIdColumnType())
