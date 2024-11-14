package me.snoty.integration.common.diff

import org.bson.Document
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class EntityDiffTest {
	@Test
	fun `diff detects creations`() {
		val document = Document("key", "value")
		val diff = document.diff(null)

		assertInstanceOf(DiffResult.Created::class.java, diff)
	}

	@Test
	fun `diff detects updates`() {
		var document = Document("key", "value")
		val result = document.diff(Document())
		assertIs<DiffResult.Updated>(result)
		result.change.apply {
			assertEquals(1, size)
			val change = assertIs<Change<*, *>>(values.first())
			assertEquals("value", change.new)
			assertNull(change.old)
		}

		document = Document("key", "new")
		val other = Document("key", "old")
		val result2 = document.diff(other)
		assertIs<DiffResult.Updated>(result2)
		result2.change.apply {
			assertEquals(1, size)
			val change = assertIs<Change<*, *>>(values.first())
			assertEquals("old", change.old)
			assertEquals("new", change.new)
		}

		document = Document("key", "value")
		val other2 = Document("key2", "value")
		val result3 = document.diff(other2)
		assertIs<DiffResult.Updated>(result3)
		result3.change.apply {
			assertEquals(2, size)

			assertAny(values) {
				it.old == "value" && it.new == null
			}
			assertAny(values) {
				it.old == null && it.new == "value"
			}
		}
	}
}

// TODO: use test fixtures
inline fun <T> assertAny(items: Collection<T>, assertion: (T) -> Boolean): T {
	if (items.isEmpty()) throw AssertionError("List is empty")
	val matching = items.firstOrNull(assertion)
	assertNotNull(matching)
	return matching!!
}
