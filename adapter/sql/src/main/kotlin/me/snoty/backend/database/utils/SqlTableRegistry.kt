package me.snoty.backend.database.utils

import me.snoty.backend.injection.getFromAllScopes
import org.jetbrains.exposed.v1.core.Table
import org.koin.core.Koin
import org.koin.core.annotation.Single

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
annotation class InternalSqlApi

interface SqlTableRegistry {
    fun getTables(): List<Table>
    fun getEntityStateTables(): List<EntityStateTable>
    fun getNodePersistenceTables(): List<NodePersistenceTable<*>>
}

@Single
@OptIn(InternalSqlApi::class)
class SqlTableRegistryImpl(private val koin: Koin) : SqlTableRegistry {
    override fun getTables(): List<Table> =
        koin.getFromAllScopes<Table>() +
        getEntityStateTables() +
        getNodePersistenceTables()

    override fun getEntityStateTables(): List<EntityStateTable> = entityStateTables

    override fun getNodePersistenceTables(): List<NodePersistenceTable<*>> = nodePersistenceTables
}
