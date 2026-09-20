package me.snoty.node.diff

import me.snoty.backend.test.randomString
import me.snoty.backend.wiring.node.state.Change
import me.snoty.backend.wiring.node.state.DiffResult
import me.snoty.backend.wiring.node.state.EntityState
import me.snoty.backend.wiring.node.state.checksum
import org.bson.Document
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiffNodeHandlerTest {
	private val id = randomString()
	private val excluded = "raw"

	private fun getDocs() = Document(mapOf(
		"first" to 1,
		excluded to "exists",
	)) to Document(mapOf(
		"first" to 1,
	))

	@Test
	fun `test processStates exclusion no change`() {
		val (old, new) = getDocs()

		val (_, newStates) = processStates(
			oldStates = mapOf(id to EntityState(id, old)),
			newData = DiffNodeHandler.Data(mapOf(id to new)),
			excludedFields = listOf(excluded),
		)

		val change = newStates.values.single()
		assertEquals(DiffResult.Unchanged, change.diffResult)
	}

	@Test
	fun `test processStates exclusion new field`() {
		val (old, new) = getDocs()

		val key = "mykey"
		val value = "someValue"
		new[key] = value

		val (_, newStates) = processStates(
			oldStates = mapOf(id to EntityState(id, old)),
			newData = DiffNodeHandler.Data(mapOf(id to new)),
			excludedFields = listOf(excluded),
		)

		val change = newStates.values.single()
		assertEquals(
			DiffResult.Updated(
				checksum = new.checksum(), change = mapOf(key to Change<String, String>(null, value))
			), change.diffResult
		)
	}
}
