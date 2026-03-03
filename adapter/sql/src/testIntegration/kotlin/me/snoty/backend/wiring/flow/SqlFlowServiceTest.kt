package me.snoty.backend.wiring.flow

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.wiring.node.NodeConnectionTable
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.core.FlowId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.flow.FlowService
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
