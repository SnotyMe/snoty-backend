package me.snoty.backend.wiring.node

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.snoty.backend.database.sql.utils.nodeId
import me.snoty.backend.database.sql.utils.nodeType
import me.snoty.backend.database.sql.utils.rawJsonb
import me.snoty.backend.database.sql.utils.userId
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.core.node.NodeId
import me.snoty.core.node.StandaloneNode
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.tryDeserializeNodeSettings
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import kotlin.uuid.Uuid

@Single(binds = [Table::class])
class NodeTable(flowTable: FlowTable) : IdTable<NodeId>("node") {
	override val id = nodeId("id").clientDefault {
		NodeId(Uuid.generateV7().toString())
	}.entityId()
	override val primaryKey = PrimaryKey(id)

	val flowId = reference("flow_id", flowTable, onDelete = ReferenceOption.CASCADE)
	val userId = userId("user_id")

	@Deprecated("Use type instead - kept mapped to prevent data loss until the new mapping is verified", level = DeprecationLevel.ERROR)
	private val descriptor_namespace = text("descriptor_namespace").nullable()
	@Deprecated("Use type instead - kept mapped to prevent data loss until the new mapping is verified", level = DeprecationLevel.ERROR)
	private val descriptor_name = text("descriptor_name").nullable()

	val type = nodeType("type")
	val name = text("name")

	val logLevel = enumerationByName("log_level", 10, Level::class).nullable()
	val positionX = integer("position_x")
	val positionY = integer("position_y")
	val width = integer("width")
	val height = integer("height")
	@OptIn(InternalSerializationApi::class)
	val settings = rawJsonb<NodeSettings>("settings")

	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val modifiedAt = timestamp("modified_at").defaultExpression(CurrentTimestamp)
}

@OptIn(InternalSerializationApi::class)
fun ResultRow.toStandalone(nodeTable: NodeTable, json: Json, nodeRegistry: NodeRegistry): StandaloneNode {
	return StandaloneNode(
		id = this[nodeTable.id].value,
		flowId = this[nodeTable.flowId].value,
		userId = this[nodeTable.userId],
		type = this[nodeTable.type],
		name = this[nodeTable.name],
		logLevel = this[nodeTable.logLevel],
		position = NodePosition(
			x = this[nodeTable.positionX],
			y = this[nodeTable.positionY],
			width = this[nodeTable.width],
			height = this[nodeTable.height]
		),
		createdAt = this[nodeTable.createdAt],
		modifiedAt = this[nodeTable.modifiedAt],
		settings = tryDeserializeNodeSettings(this[nodeTable.type], nodeRegistry) {
			json.decodeFromString(it.serializer(), this[nodeTable.settings])
		}
	)
}

@Single(binds = [Table::class])
class NodeConnectionTable(nodeTable: NodeTable) : Table("node_connection") {
	val from = reference("from", nodeTable, onDelete = ReferenceOption.CASCADE)
	val to = reference("to", nodeTable, onDelete = ReferenceOption.CASCADE)
	override val primaryKey = PrimaryKey(from, to)
}
