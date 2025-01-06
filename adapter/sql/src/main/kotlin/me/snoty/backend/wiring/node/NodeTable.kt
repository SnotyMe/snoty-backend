package me.snoty.backend.wiring.node

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.utils.hackyEncodeToString
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single(binds = [Table::class])
class NodeTable(json: Json) : UuidTable("node") {
	val flowId = reference("flow_id", FlowTable)
	val userId = uuid("user_id")

	val descriptor_namespace = varchar("descriptor_namespace", 255)
	val descriptor_name = varchar("descriptor_name", 255)

	val logLevel = enumerationByName("log_level", 10, Level::class)
	@OptIn(InternalSerializationApi::class)
	val settings = jsonb<NodeSettings>("settings", { settings ->
		json.hackyEncodeToString(settings)
	}, {
		json.decodeFromString(it)
	})

	val standaloneColumns = listOf(id, flowId, userId, descriptor_namespace, descriptor_name, logLevel, settings)
}

fun NodeTable.selectStandalone() = select(standaloneColumns)
fun ResultRow.toStandalone(NodeTable: NodeTable) = StandaloneNode(
	_id = this[NodeTable.id].value.toString(),
	flowId = this[NodeTable.flowId].value.toString(),
	userId = this[NodeTable.userId],
	descriptor = NodeDescriptor(namespace = this[NodeTable.descriptor_namespace], name = this[NodeTable.descriptor_name]),
	logLevel = this[NodeTable.logLevel],
	settings = this[NodeTable.settings],
)

@Single(binds = [Table::class])
class NodeConnectionTable(NodeTable: NodeTable) : Table("node_connection") {
	val from = reference("from", NodeTable, onDelete = ReferenceOption.CASCADE)
	val to = reference("to", NodeTable, onDelete = ReferenceOption.CASCADE)

	override val primaryKey = PrimaryKey(from, to)
}
