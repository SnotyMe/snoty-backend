package me.snoty.backend.wiring.node

import io.mockk.mockk
import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.utils.randomV7
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.backend.wiring.flow.SqlFlowService
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.snotyJson
import org.jetbrains.exposed.sql.SchemaUtils
import kotlin.uuid.Uuid

class SqlNodeServiceTest : NodeServiceSpec() {
	private val nodeTable = NodeTable()
	private val nodeConnectionTable = NodeConnectionTable(nodeTable)

	private val db = PostgresTest.getPostgresDatabase {
		SchemaUtils.create(FlowTable, nodeTable, nodeConnectionTable)
	}

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
	)

	override val makeId = suspend  {
		flowService.create(
			userId = Uuid.randomV7(),
			name = "test",
		)._id
	}
}
