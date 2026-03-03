package me.snoty.backend.database.utils

import me.snoty.backend.database.sql.SanitizedPrimaryKey
import me.snoty.backend.database.sql.sqlTableName
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Factory

@InternalSqlApi
val entityStateTables: List<EntityStateTable>
	field = mutableListOf()

@Factory
@OptIn(InternalSqlApi::class)
class EntityStateTable(descriptor: NodeDescriptor, nodeTable: NodeTable) : Table(descriptor.sqlTableName("states")) {
	init {
		entityStateTables += this
	}

	val nodeId = reference("node_id", nodeTable, onDelete = ReferenceOption.CASCADE)
	val entityId = text("entity_id")

	override val primaryKey = SanitizedPrimaryKey(nodeId, entityId)

	val state = text("state")
	val checksum = long("checksum")

	fun selectStandalone() = select(entityId, state, checksum)
}

fun ResultRow.toEntityState(entityStateTable: EntityStateTable, codecRegistry: CodecRegistry): EntityState = EntityState(
	id = this[entityStateTable.entityId],
	state = Document.parse(this[entityStateTable.state], codecRegistry[Document::class.java]),
	checksum = this[entityStateTable.checksum]
)
