package me.snoty.backend.wiring.node

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.database.sql.utils.kotlinUuid
import me.snoty.backend.database.sql.utils.rawJsonb
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.tryDeserializeNodeSettings
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single(binds = [Table::class])
class NodeTable(flowTable: FlowTable) : UuidTable("node") {
	val flowId = reference("flow_id", flowTable)
	val userId = kotlinUuid("user_id")

	val descriptor_namespace = varchar("descriptor_namespace", 255)
	val descriptor_name = varchar("descriptor_name", 255)

	val logLevel = enumerationByName("log_level", 10, Level::class).nullable()
	@OptIn(InternalSerializationApi::class)
	val settings = rawJsonb<NodeSettings>("settings")

	val standaloneColumns = listOf(id, flowId, userId, descriptor_namespace, descriptor_name, logLevel, settings)
}

fun NodeTable.selectStandalone() = select(standaloneColumns)
@OptIn(InternalSerializationApi::class)
fun ResultRow.toStandalone(nodeTable: NodeTable, json: Json, nodeRegistry: NodeRegistry): StandaloneNode {
	val descriptor = NodeDescriptor(namespace = this[nodeTable.descriptor_namespace], name = this[nodeTable.descriptor_name])

	return StandaloneNode(
		_id = this[nodeTable.id].value.toString(),
		flowId = this[nodeTable.flowId].value.toString(),
		userId = this[nodeTable.userId],
		descriptor = descriptor,
		logLevel = this[nodeTable.logLevel],
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
