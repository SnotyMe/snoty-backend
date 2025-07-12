package me.snoty.backend.utils.bson

import me.snoty.backend.test.TestCodecRegistry
import me.snoty.integration.common.utils.bsonTypeClassMap
import org.bson.Document
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BsonUtilsTest {
	@Test
	fun `parseArray documents`() {
		val expected = listOf(
			Document(mapOf("login" to "Test1", "id" to 1337)),
			Document(mapOf("login" to "Test2", "id" to 420)),
		)
		val json = """
			[
				{"login": "Test1", "id": 1337},
				{"login": "Test2", "id": 420}
			]
		""".trimIndent()
		val parsed = parseArray(json, TestCodecRegistry, bsonTypeClassMap())

		assertEquals(expected, parsed)
	}

	@Test
	fun `parseArray mixed types`() {
		val instant = Instant.fromEpochMilliseconds(13371337)
		val expected = listOf(
			3.14,
			"yes",
			Document("key", listOf("hello", Document("world", true))),
			instant.toString() // ISO-8601
		)
		val json = """
			[3.14, "yes", {"key": ["hello", {"world": true}]}, "$instant"]
		""".trimIndent()
		val parsed = parseArray(json, TestCodecRegistry, bsonTypeClassMap())

		assertEquals(expected, parsed)
	}
}
