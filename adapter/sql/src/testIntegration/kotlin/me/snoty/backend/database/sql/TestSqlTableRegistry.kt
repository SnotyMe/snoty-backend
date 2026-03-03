package me.snoty.backend.database.sql

import me.snoty.backend.database.utils.EntityStateTable
import me.snoty.backend.database.utils.NodePersistenceTable
import me.snoty.backend.database.utils.SqlTableRegistry
import org.jetbrains.exposed.v1.core.Table

class TestSqlTableRegistry(
    private vararg val tables: Table,
) : SqlTableRegistry {
    override fun getTables(): List<Table> = tables.toList()
    override fun getEntityStateTables(): List<EntityStateTable> = tables.filterIsInstance<EntityStateTable>()
    override fun getNodePersistenceTables(): List<NodePersistenceTable<*>> = tables.filterIsInstance<NodePersistenceTable<*>>()
}
