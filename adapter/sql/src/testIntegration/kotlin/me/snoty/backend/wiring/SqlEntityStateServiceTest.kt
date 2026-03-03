package me.snoty.backend.wiring

import io.mockk.mockk
import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.database.sql.TestSqlTableRegistry
import me.snoty.backend.database.sql.migrations.impl.`V0_2_0_1772393750__entity-state`
import me.snoty.backend.database.utils.EntityStateTable
import me.snoty.backend.database.utils.SqlEntityStateService
import me.snoty.backend.utils.bson.CodecRegistryProvider
import me.snoty.backend.utils.bson.provideApiCodec
import me.snoty.backend.utils.bson.provideCodecRegistry
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.backend.wiring.flow.SqlFlowService
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.core.NodeId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.provideStateCodecRegistry
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.wiring.flow.FlowService
import kotlin.uuid.Uuid

class SqlEntityStateServiceTest : EntityStateServiceSpec({ NodeId(Uuid.random().toString()) }) {
	private val flowTable = FlowTable(snotyJson {})
	private val nodeTable = NodeTable(flowTable)
	private val entityStateTable = EntityStateTable(nodeDescriptor, nodeTable)

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
