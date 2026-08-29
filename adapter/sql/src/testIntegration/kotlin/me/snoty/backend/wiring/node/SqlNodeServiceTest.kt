package me.snoty.backend.wiring.node

import io.mockk.mockk
import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.backend.wiring.flow.SqlFlowService
import me.snoty.core.flow.WorkflowSettings
import me.snoty.core.user.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.snotyJson
import kotlin.uuid.Uuid

class SqlNodeServiceTest : NodeServiceSpec() {
	private val db = PostgresTest.getPostgresDatabase {}

	val flowTable = FlowTable(snotyJson {})
	private val nodeTable = NodeTable(flowTable)
	private val nodeConnectionTable = NodeConnectionTable(nodeTable)

	override val service: NodeService = SqlNodeService(
		db = db,
		json = snotyJson {},
		nodeRegistry = nodeRegistry,
		nodeTable = nodeTable,
		nodeConnectionTable = nodeConnectionTable,
	)

	private val flowService = SqlFlowService(
		db = db,
		flowScheduler = mockk(relaxed = true),
		nodeService = service,
		flowTable = flowTable,
	)

	override val makeFlowId = suspend  {
		flowService.create(
			userId = UserId(Uuid.generateV7().toString()),
			name = "test",
			settings = WorkflowSettings(),
		).id
	}
}
