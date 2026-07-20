package me.snoty.backend.wiring.node

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.snoty.backend.database.sql.utils.nodeId
import me.snoty.backend.database.sql.utils.rawJsonb
import me.snoty.backend.database.sql.utils.userId
import me.snoty.backend.utils.randomV7
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.core.NodeId
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.*
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import kotlin.uuid.Uuid

@Single(binds = [Table::class])
class NodeTable(flowTable: FlowTable) : IdTable<NodeId>("node") {
	override val id = nodeId("id").clientDefault {
		NodeId(Uuid.randomV7().toString())
	}.entityId()
	override val primaryKey = PrimaryKey(id)

	val flowId = reference("flow_id", flowTable, onDelete = ReferenceOption.CASCADE)
	val userId = userId("user_id")

	val descriptor_namespace = text("descriptor_namespace")
	val descriptor_name = text("descriptor_name")

	val logLevel = enumerationByName("log_level", 10, Level::class).nullable()
	val positionX = integer("position_x")
	val positionY = integer("position_y")
	val width = integer("width")
	val height = integer("height")
	@OptIn(InternalSerializationApi::class)
	val settings = rawJsonb<NodeSettings>("settings")
}

@OptIn(InternalSerializationApi::class)
fun ResultRow.toStandalone(nodeTable: NodeTable, json: Json, nodeRegistry: NodeRegistry): StandaloneNode {
	val descriptor = NodeDescriptor(namespace = this[nodeTable.descriptor_namespace], name = this[nodeTable.descriptor_name])

	return StandaloneNode(
		_id = this[nodeTable.id].value,
		flowId = this[nodeTable.flowId].value,
		userId = this[nodeTable.userId],
		descriptor = descriptor,
		logLevel = this[nodeTable.logLevel],
		position = NodePosition(
			x = this[nodeTable.positionX],
			y = this[nodeTable.positionY],
			width = this[nodeTable.width],
			height = this[nodeTable.height]
		),
		settings = tryDeserializeNodeSettings(descriptor, nodeRegistry) {
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
