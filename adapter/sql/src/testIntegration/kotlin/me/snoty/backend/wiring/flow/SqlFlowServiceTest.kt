package me.snoty.backend.wiring.flow

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.flow.FlowService
import kotlin.uuid.Uuid

class SqlFlowServiceTest : FlowServiceSpec({ Uuid.random().toHexString() }) {
	val db = PostgresTest.getPostgresDatabase {}

	override val nodeService: NodeService = SqlNodeService(db, nodeRegistry, snotyJson {})
	override val service: FlowService = SqlFlowService(db, flowScheduler, nodeService)
}
