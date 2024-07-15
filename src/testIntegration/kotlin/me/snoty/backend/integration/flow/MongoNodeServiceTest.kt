package me.snoty.backend.integration.flow

import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.TestIds.USER_ID_1
import me.snoty.backend.test.TestIds.USER_ID_CONTROL
import me.snoty.backend.test.TestNodeHandler
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.Logger

class MongoNodeServiceTest {
	private val descriptor = NodeDescriptor(
		subsystem = "mysystem",
		type = "mytype"
	)

	private val db = MongoTest.getMongoDatabase {}
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(descriptor, object : TestNodeHandler() {
			override val position = NodePosition.START

			context(NodeHandlerContext, EmitNodeOutputContext)
			override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {}
		})
	}
	private val service = MongoNodeService(db, nodeRegistry, mockk(relaxed = true)) {
		EmptyNodeSettings()
	}

	@Test
	fun `test getByUser`(): Unit = runBlocking {
		val nodeId = service.create(USER_ID_1, descriptor, EmptyNodeSettings())

		val byUser = service.getByUser(USER_ID_1, NodePosition.START).toList()
		assertEquals(1, byUser.size)
		assertEquals(nodeId, byUser[0]._id)

		val byNotStart = service.getByUser(USER_ID_1, NodePosition.MIDDLE).toList()
		assertEquals(0, byNotStart.size)

		val byWrongUser = service.getByUser(USER_ID_CONTROL, NodePosition.START).toList()
		assertEquals(0, byWrongUser.size)

		val byWrongUserNotStart = service.getByUser(USER_ID_CONTROL, NodePosition.MIDDLE).toList()
		assertEquals(0, byWrongUserNotStart.size)
	}
}
