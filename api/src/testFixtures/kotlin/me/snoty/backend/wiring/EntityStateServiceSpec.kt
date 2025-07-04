package me.snoty.backend.wiring

import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.test.TestIds.INTEGRATION_NAME
import me.snoty.backend.test.TestIds.USER_ID_1
import me.snoty.backend.utils.bson.getIdAsString
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.checksum
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.testcontainers.utility.Base58.randomString
import kotlin.test.assertNotNull
import kotlin.time.Instant

abstract class EntityStateServiceSpec(val makeId: () -> NodeId) {
	abstract val service: EntityStateService
	abstract val nodeService: NodeService
	abstract val flowService: FlowService

	protected val nodeDescriptor = NodeDescriptor(javaClass.packageName, INTEGRATION_NAME)
	private val flowId by lazy { runBlocking { flowService.create(USER_ID_1, nodeDescriptor.name, WorkflowSettings()) }._id }
	private fun flowNode(): Node = runBlocking {
		nodeService.create(
			flowId = flowId,
			userID = USER_ID_1,
			descriptor = nodeDescriptor,
			settings = EmptyNodeSettings(),
		)
	}

	@Test
	fun `test nothing`() = runBlocking {
		val test = assertDoesNotThrow {
			service.getLastState(makeId(), randomString(32))
		}
		assertNull(test)
	}

	@Test
	fun `test updateStates insert`() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = Document("id", 10).append("date", date)
		val node = flowNode()
		service.updateState(
			node._id,
			entity,
			DiffResult.Created(fields = entity, checksum = entity.checksum()),
		)

		val createdEntity = assertDoesNotThrow {
			service.getLastState(node._id, entity.getIdAsString()!!)
		}
		assertNotNull(createdEntity)
		assertEquals(entity.getIdAsString(), createdEntity.id)
		assertEquals(entity.checksum(), createdEntity.checksum)
		assertEquals(entity, createdEntity.state)
	}

	@Test
	fun `test updateStates update`() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = Document("id", 10).append("date", date)
		val node = flowNode()
		service.updateState(
			node._id,
			entity,
			DiffResult.Created(fields = entity, checksum = entity.checksum()),
		)

		val createdEntity = assertDoesNotThrow {
			service.getLastState(node._id, entity.getIdAsString()!!)
		}
		assertNotNull(createdEntity)
		assertEquals(entity.getIdAsString(), createdEntity.id)
		assertEquals(entity.checksum(), createdEntity.checksum)
		assertEquals(entity, createdEntity.state)

		val date2 = Instant.fromEpochMilliseconds(2000)
		val entity2 = Document(entity).append("date", date2)
		service.updateState(
			node._id,
			entity2,
			DiffResult.Updated(
				change = mapOf("date" to Change(old = date, new = date2)),
				checksum = entity2.checksum(),
			),
		)

		val updatedEntity = assertDoesNotThrow {
			service.getLastState(node._id, entity.getIdAsString()!!)
		}

		assertNotNull(updatedEntity)
		assertEquals(entity.getIdAsString(), updatedEntity.id)
		assertEquals(entity2, updatedEntity.state)
		assertEquals(entity2.checksum(), updatedEntity.checksum)
	}
}
