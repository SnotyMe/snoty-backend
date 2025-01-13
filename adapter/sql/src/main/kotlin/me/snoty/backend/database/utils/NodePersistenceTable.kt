package me.snoty.backend.database.utils

import me.snoty.backend.database.sql.SanitizedPrimaryKey
import me.snoty.backend.database.sql.sqlTableName
import me.snoty.backend.database.sql.utils.rawJsonb
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import kotlin.uuid.Uuid

class NodePersistenceTable<T : Any>(
	descriptor: NodeDescriptor,
	name: String,
	nodeTable: NodeTable
) : IdTable<Uuid>(descriptor.sqlTableName(name)) {
	override val id = reference("node_id", nodeTable, onDelete = ReferenceOption.CASCADE)
	val entityId = varchar("entity_id", 255)
	override val primaryKey = SanitizedPrimaryKey(id, entityId)

	val entity = rawJsonb<T>("entity")

	fun selectStandalone() = select(entityId)
}
