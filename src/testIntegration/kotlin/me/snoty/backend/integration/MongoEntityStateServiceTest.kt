package me.snoty.backend.integration

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.mockk
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.assertCombinations
import me.snoty.backend.test.assertInstanceOf
import me.snoty.backend.test.getField
import me.snoty.integration.common.IntegrationDescriptor
import me.snoty.integration.common.diff.*
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
		const val INSTANCE_ID = "myinstance"
		const val INSTANCE_ID_CONTROL = "notmyinstance"
		const val ENTITY_TYPE = "exam"
		const val ENTITY_TYPE_CONTROL = "notexam"
	}

	private val mongoDB = MongoTest.getDatabase()
	private val service = MongoEntityStateService(
		mongoDB,
		IntegrationDescriptor(INTEGRATION_NAME),
		SimpleMeterRegistry(),
		mockk()
	)

	@Test
	fun test_nothing() = runBlocking {
		val test = assertDoesNotThrow {
			service.getEntities(UUID.randomUUID(), INSTANCE_ID, ENTITY_TYPE)
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
	fun test_updateStates_insert() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = MyEntity(10L, date)
		service.updateStates(USER_ID_1, INSTANCE_ID, listOf(entity))

		val createdEntitiesFlow = assertDoesNotThrow {
			service.getEntities(USER_ID_1, INSTANCE_ID, ENTITY_TYPE)
		}
		val createdEntities = createdEntitiesFlow.toList()
		assertEquals(1, createdEntities.size)
		val createdEntity = createdEntities.first()
		assertEquals(entity.id, createdEntity.id.toLong())
		assertEquals(entity.checksum, createdEntity.checksum)
		// mutates
		entity.prepareFieldsForDiff(createdEntity.state)
		assertEquals(entity.fields, createdEntity.state)

		assertCombinations(
			service::getEntities,
			listOf(USER_ID_1, USER_ID_CONTROL),
			listOf(INSTANCE_ID, INSTANCE_ID_CONTROL),
			listOf(ENTITY_TYPE, ENTITY_TYPE_CONTROL),
			exclude = listOf(USER_ID_1, INSTANCE_ID, ENTITY_TYPE)
		) { create ->
			val entitiesFlow = assertDoesNotThrow {
				create()
			}
			assertEquals(0, entitiesFlow.count())
		}
	}

	@Test
	fun test_updateStates_update() = runBlocking {
		val date = Instant.fromEpochMilliseconds(1000)
		val entity = MyEntity(10L, date)
		service.updateStates(USER_ID_2, INSTANCE_ID, listOf(entity))

		val createdEntitiesFlow = assertDoesNotThrow {
			service.getEntities(USER_ID_2, INSTANCE_ID, ENTITY_TYPE)
		}
		val createdEntities = createdEntitiesFlow.toCollection(ArrayList())
		assertEquals(1, createdEntities.size)
		val createdEntity = createdEntities.first
		assertEquals(entity.id, createdEntity.id.toLong())
		assertEquals(entity.checksum, createdEntity.checksum)
		// mutates
		entity.prepareFieldsForDiff(createdEntity.state)
		assertEquals(entity.fields, createdEntity.state)

		val date2 = Instant.fromEpochMilliseconds(2000)
		val entity2 = entity.copy(date=date2)
		service.updateStates(USER_ID_2, INSTANCE_ID, listOf(entity2))

		val userEntityChanges = service.getField<MongoCollection<UserEntityChanges>>("userEntityChanges")

		val descriptor = Descriptor(INSTANCE_ID, ENTITY_TYPE, entity.id.toString(), USER_ID_2)

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
