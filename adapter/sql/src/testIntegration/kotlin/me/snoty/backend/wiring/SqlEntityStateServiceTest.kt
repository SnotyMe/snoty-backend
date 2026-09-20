package me.snoty.backend.wiring

import io.mockk.mockk
import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.database.sql.TestSqlTableRegistry
import me.snoty.backend.database.sql.migrations.impl.`V0_2_0_1772393750__entity-state`
import me.snoty.backend.database.utils.EntityStateTable
import me.snoty.backend.database.utils.SqlEntityStateService
import me.snoty.backend.utils.bson.CodecRegistryProvider
import me.snoty.backend.utils.bson.bsonTypeClassMap
import me.snoty.backend.utils.bson.provideApiCodec
import me.snoty.backend.utils.bson.provideCodecRegistry
import me.snoty.backend.utils.snotyJson
import me.snoty.backend.wiring.flow.FlowService
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.backend.wiring.flow.SqlFlowService
import me.snoty.backend.wiring.node.NodeService
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.backend.wiring.node.state.EntityStateService
import me.snoty.backend.wiring.node.state.provideStateCodecRegistry
import me.snoty.core.node.NodeId
import kotlin.uuid.Uuid

class SqlEntityStateServiceTest : EntityStateServiceSpec({ NodeId(Uuid.random().toString()) }) {
	private val flowTable = FlowTable(snotyJson {})
	private val nodeTable = NodeTable(flowTable)
	private val entityStateTable = EntityStateTable(nodeType, nodeTable)

	private val db = PostgresTest.getPostgresDatabase(
		extraMigrations = listOf(
			`V0_2_0_1772393750__entity-state`(TestSqlTableRegistry(entityStateTable))
		)
	) {}

	val bsonTypeClassMap = bsonTypeClassMap()
	private val codecRegistry = provideCodecRegistry(CodecRegistryProvider(provideStateCodecRegistry(bsonTypeClassMap, provideApiCodec(bsonTypeClassMap).registry)))
	override val nodeService: NodeService = SqlNodeService(
		db = db,
		nodeTable = nodeTable,
		json = snotyJson {},
		nodeRegistry = mockk(),
		nodeConnectionTable = mockk(),
	)
	override val flowService: FlowService = SqlFlowService(db, mockk(relaxed = true), nodeService, flowTable)
	override val service: EntityStateService = SqlEntityStateService(db, codecRegistry, entityStateTable)
}
