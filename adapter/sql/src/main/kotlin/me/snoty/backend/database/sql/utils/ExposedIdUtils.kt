package me.snoty.backend.database.sql.utils

import me.snoty.backend.utils.toUuid
import me.snoty.core.FlowId
import me.snoty.core.NodeId
import me.snoty.core.UserId
import org.jetbrains.exposed.v1.core.*

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

class FlowIdColumnType : ColumnType<FlowId>() {
    private val delegate = UuidColumnType()

    override fun sqlType() = delegate.sqlType()

    override fun valueFromDB(value: Any): FlowId =
        FlowId(delegate.valueFromDB(value).toString())

    override fun notNullValueToDB(value: FlowId): Any =
        delegate.notNullValueToDB(value.value.toUuid())
}

fun Table.flowId(name: String): Column<FlowId> =
    registerColumn(name, FlowIdColumnType())

class NodeIdColumnType : ColumnType<NodeId>() {
    private val delegate = UuidColumnType()

    override fun sqlType() = delegate.sqlType()

    override fun valueFromDB(value: Any): NodeId =
        NodeId(delegate.valueFromDB(value).toString())

    override fun notNullValueToDB(value: NodeId): Any =
        delegate.notNullValueToDB(value.value.toUuid())
}

fun Table.nodeId(name: String): Column<NodeId> =
    registerColumn(name, NodeIdColumnType())
