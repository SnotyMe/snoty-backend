package me.snoty.backend.wiring.flow

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.wiring.node.NodeConnectionTable
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.flow.FlowService
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import kotlin.uuid.Uuid

class SqlFlowServiceTest : FlowServiceSpec({ Uuid.random().toString() }) {
	private val flowTable = FlowTable(snotyJson {})
	val nodeTable = NodeTable(flowTable)
	val nodeConnectionTable = NodeConnectionTable(nodeTable)

	val db = PostgresTest.getPostgresDatabase {
		SchemaUtils.create(nodeTable, nodeConnectionTable)
	}

	override val nodeService: NodeService = SqlNodeService(
		db = db,
		json = snotyJson {},
		nodeRegistry = nodeRegistry,
		nodeTable = nodeTable,
		nodeConnectionTable = nodeConnectionTable,
	)
	override val service: FlowService = SqlFlowService(db, flowScheduler, nodeService, flowTable)
}
