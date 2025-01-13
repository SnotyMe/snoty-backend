package me.snoty.backend.database.utils

import me.snoty.backend.wiring.node.NodeTable
import me.snoty.integration.common.diff.state.EntityState
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.koin.core.annotation.Single

@Single
class EntityStateTable(nodeTable: NodeTable) : Table() {
	val nodeId = reference("node_id", nodeTable, onDelete = ReferenceOption.CASCADE).index()
	val entityId = varchar("entity_id", 255)

	override val primaryKey = PrimaryKey(nodeId, entityId)

	val state = varchar("state", 255)
	val checksum = long("checksum")

	fun selectStandalone() = select(entityId, state, checksum)
}

fun ResultRow.toEntityState(entityStateTable: EntityStateTable, codecRegistry: CodecRegistry): EntityState = EntityState(
	id = this[entityStateTable.entityId],
	state = Document.parse(this[entityStateTable.state], codecRegistry[Document::class.java]),
	checksum = this[entityStateTable.checksum]
)
