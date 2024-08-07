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
import me.snoty.backend.test.TestNodeMetadata
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
		registerHandler(descriptor, TestNodeMetadata.copy(position = NodePosition.START), NoOpNodeHandler)
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
