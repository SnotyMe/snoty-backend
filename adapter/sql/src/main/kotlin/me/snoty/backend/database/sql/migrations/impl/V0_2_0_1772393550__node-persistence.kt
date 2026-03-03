package me.snoty.backend.database.sql.migrations.impl

import me.snoty.backend.database.utils.SqlTableRegistry
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.v1.core.InternalApi
import org.koin.core.annotation.Single

@Single
@Suppress("ClassName")
class `V0_2_0_1772393550__node-persistence`(private val sqlTableRegistry: SqlTableRegistry) : BaseJavaMigration() {
    @OptIn(InternalApi::class)
    override fun migrate(context: Context): Unit = context.connection.createStatement().use { stmt ->
        sqlTableRegistry.getNodePersistenceTables().forEach { table ->
            stmt.addBatch(
                """
                    CREATE TABLE ${table.tableName}
                    (
                        "node_id" uuid
                            CONSTRAINT "fk_${table.tableNameWithoutSchemeSanitized}_node_id__id" REFERENCES "node" ("id") ON DELETE CASCADE ON UPDATE RESTRICT,
                        "entity_id" TEXT,
                        "entity" JSONB NOT NULL,
                        PRIMARY KEY ("node_id", "entity_id")
                    )
                """.trimIndent()
            )
        }
        stmt.executeBatch()
    }
}
