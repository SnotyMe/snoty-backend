package me.snoty.backend.database.utils

import me.snoty.backend.database.sql.SanitizedPrimaryKey
import me.snoty.backend.database.sql.sqlTableName
import me.snoty.backend.database.sql.utils.rawJsonb
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.core.node.NodeType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

@InternalSqlApi
val nodePersistenceTables: List<NodePersistenceTable<*>>
	field = mutableListOf()

@OptIn(InternalSqlApi::class)
class NodePersistenceTable<T : Any>(
	nodeType: NodeType,
	name: String,
	nodeTable: NodeTable
) : Table(nodeType.sqlTableName(name)) {
	init {
		nodePersistenceTables += this
	}

	val nodeId = reference("node_id", nodeTable, onDelete = ReferenceOption.CASCADE)
	val entityId = text("entity_id")
	override val primaryKey = SanitizedPrimaryKey(nodeId, entityId)

	val entity = rawJsonb<T>("entity")
}
