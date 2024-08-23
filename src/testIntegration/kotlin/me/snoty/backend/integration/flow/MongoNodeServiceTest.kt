package me.snoty.backend.integration.flow

import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.TestIds.USER_ID_1
import me.snoty.backend.test.TestIds.USER_ID_CONTROL
import me.snoty.backend.test.nodeMetadata
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MongoNodeServiceTest {
	private val descriptor = NodeDescriptor(
		subsystem = "mysystem",
		type = "mytype"
	)

	private val db = MongoTest.getMongoDatabase {}
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(descriptor = descriptor, position = NodePosition.START), NoOpNodeHandler)
	}
	private val service = MongoNodeService(db, nodeRegistry) {
		EmptyNodeSettings()
	}

	@Test
	fun `test getByUser`(): Unit = runBlocking {
		val createdNode = service.create(USER_ID_1, descriptor, EmptyNodeSettings())

		val byUser = service.query(USER_ID_1, NodePosition.START).toList()
		assertEquals(1, byUser.size)
		assertEquals(createdNode, byUser[0])

		val byNotStart = service.query(USER_ID_1, NodePosition.MIDDLE).toList()
		assertEquals(0, byNotStart.size)

		val byWrongUser = service.query(USER_ID_CONTROL, NodePosition.START).toList()
		assertEquals(0, byWrongUser.size)

		val byWrongUserNotStart = service.query(USER_ID_CONTROL, NodePosition.MIDDLE).toList()
		assertEquals(0, byWrongUserNotStart.size)
	}
}
