package me.snoty.backend.integration

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import me.snoty.backend.database.mongo.getIdAsString
import me.snoty.backend.dev.randomString
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.node
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.TestIds.INTEGRATION_NAME
import me.snoty.backend.test.TestIds.USER_ID_1
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.checksum
import me.snoty.integration.common.diff.provideStateCodecRegistry
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertNotNull

class MongoEntityStateServiceTest {
	private val mongoDB = MongoTest.getMongoDatabase {}
	private val nodeDescriptor = NodeDescriptor(Subsystem.INTEGRATION, INTEGRATION_NAME)
	private val service = MongoEntityStateService(
		mongoDB,
		nodeDescriptor,
		meterRegistry = SimpleMeterRegistry(),
		metricsPool = mockk(relaxed = true),
		codecRegistry = provideStateCodecRegistry(bsonTypeClassMap(), mongoDB.codecRegistry),
	)

	private fun flowNode(): Node = node(
		userId = USER_ID_1,
		descriptor = nodeDescriptor,
		settings = EmptyNodeSettings()
	)

	@Test
	fun `test nothing`() = runBlocking {
		val test = assertDoesNotThrow {
			service.getLastState(NodeId(), randomString())
		}
		assertNull(test)
	}

	@Test
	fun `test updateStates insert`() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = Document("id", 10L).append("date", date)
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
		val entity = Document("id", 10L).append("date", date)
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
				diff = mapOf("date" to Change(old = date, new = date2)),
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
