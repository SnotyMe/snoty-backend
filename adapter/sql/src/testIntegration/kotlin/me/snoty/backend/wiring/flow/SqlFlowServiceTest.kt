package me.snoty.backend.wiring.flow

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.utils.snotyJson
import me.snoty.backend.wiring.node.NodeConnectionTable
import me.snoty.backend.wiring.node.NodeService
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.core.flow.FlowId
import kotlin.uuid.Uuid

class SqlFlowServiceTest : FlowServiceSpec({ FlowId(Uuid.random().toString()) }) {
	private val db = PostgresTest.getPostgresDatabase {}

	private val flowTable = FlowTable(snotyJson {})
	val nodeTable = NodeTable(flowTable)
	val nodeConnectionTable = NodeConnectionTable(nodeTable)

	override val nodeService: NodeService = SqlNodeService(
		db = db,
		json = snotyJson {},
		nodeRegistry = nodeRegistry,
		nodeTable = nodeTable,
		nodeConnectionTable = nodeConnectionTable,
	)
	override val service: FlowService = SqlFlowService(db, flowScheduler, nodeService, flowTable)
}
