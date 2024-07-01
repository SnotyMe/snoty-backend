package me.snoty.backend.integration

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.mockk
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.assertInstanceOf
import me.snoty.backend.test.getField
import me.snoty.integration.common.diff.*
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.StandaloneFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*

class MongoEntityStateServiceTest {
	companion object {
		val USER_ID_1 = UUID(1, 0)
		val USER_ID_2 = UUID(2, 0)
		val USER_ID_CONTROL = UUID(64, 0)
		const val INTEGRATION_NAME = "moodle"
		const val ENTITY_TYPE = "exam"
		const val ENTITY_TYPE_CONTROL = "notexam"
	}

	private val mongoDB = MongoTest.getMongoDatabase {}
	private val nodeDescriptor = NodeDescriptor(Subsystem.INTEGRATION, INTEGRATION_NAME)
	private val service = MongoEntityStateService(
		mongoDB,
		nodeDescriptor,
		SimpleMeterRegistry(),
		mockk()
	)

	private fun flowNode(): IFlowNode = StandaloneFlowNode(
		NodeId(),
		USER_ID_1,
		nodeDescriptor,
		Document()
	)

	@Test
	fun `test nothing`() = runBlocking {
		val test = assertDoesNotThrow {
			service.getStates(flowNode())
		}
		assertEquals(0, test.count())
	}

	data class MyEntity(
		override val id: Long,
		val date: Instant,
	) : UpdatableEntity<Long>()  {
		override val type = ENTITY_TYPE
		override fun prepareFieldsForDiff(fields: Fields) {
			fields["date"] = Instant.fromEpochMilliseconds(fields.getDate("date").time)
		}
		override val fields: Fields = buildDocument {
			put("date", date)
		}
	}

	@Test
	fun `test updateStates insert`() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = MyEntity(10L, date)
		val node = flowNode()
		service.updateStates(node, listOf(entity))

		val createdEntitiesFlow = assertDoesNotThrow {
			service.getStates(node)
		}
		val createdEntities = createdEntitiesFlow.toList()
		assertEquals(1, createdEntities.size)
		val createdEntity = createdEntities.first()
		assertEquals(entity.id, createdEntity.id.toLong())
		assertEquals(entity.checksum, createdEntity.checksum)
		// mutates
		entity.prepareFieldsForDiff(createdEntity.state)
		assertEquals(entity.fields, createdEntity.state)
	}

	@Test
	fun `test updateStates update`() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = MyEntity(10L, date)
		val node = flowNode()
		service.updateStates(node, listOf(entity))

		val createdEntitiesFlow = assertDoesNotThrow {
			service.getStates(node)
		}
		val createdEntities = createdEntitiesFlow.toList()
		assertEquals(1, createdEntities.size)
		val createdEntity = createdEntities.first()
		assertEquals(entity.id, createdEntity.id.toLong())
		assertEquals(entity.checksum, createdEntity.checksum)
		// mutates
		entity.prepareFieldsForDiff(createdEntity.state)
		assertEquals(entity.fields, createdEntity.state)

		val date2 = Instant.fromEpochMilliseconds(2000)
		val entity2 = entity.copy(date=date2)
		service.updateStates(node, listOf(entity2))

		val userEntityChanges = service.getField<EntityChangesCollection>("userEntityChanges")

		val descriptor = EntityDescriptor(node._id, ENTITY_TYPE, entity.id.toString())

		val changes = userEntityChanges
			.find(Filters.eq("descriptor", descriptor))
			.sort(Sorts.ascending("time"))
			.toList()
		assertEquals(2, changes.size)
		val created = assertInstanceOf<DiffResult.Created>(changes[0].change)
		assertEquals(created.checksum, entity.checksum)
		// clone to avoid mutations
		val oldFields = Document(created.fields)
		entity.prepareFieldsForDiff(oldFields)
		assertEquals(entity.fields, oldFields)
		val updated = assertInstanceOf<DiffResult.Updated>(changes[1].change)
		assertEquals(updated.checksum, entity2.checksum)
	}
}
