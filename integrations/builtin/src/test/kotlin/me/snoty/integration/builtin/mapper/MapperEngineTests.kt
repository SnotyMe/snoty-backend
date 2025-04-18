package me.snoty.integration.builtin.mapper

import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

abstract class MapperEngineTest(val engine: MapperEngine) {
	val logger = LoggerFactory.getLogger(MapperEngineTest::class.java)!!

	protected fun settings(vararg keys: Pair<String, String>) = MapperSettings(engine = engine, fields = keys.toMap())

	abstract fun simple()
	abstract fun dotToMap()

	@Test
	fun shouldHandleEscaping() {
		val output = engine.template(logger, settings("my.key\\.value" to "value"), Document())
		assertEquals(
			Document("my", Document("key.value", "value")),
			output
		)

		val output2 = engine.template(logger, settings("my.key\\[0]" to "value"), Document())
		assertEquals(
			Document("my", Document("key[0]", "value")),
			output2
		)

		val output3 = engine.template(logger, settings("my.key\\\\[0]" to "value"), Document())
		assertEquals(
			Document("my", Document("key\\", listOf("value"))),
			output3
		)
	}

	@Test
	fun shouldHandleNestedIndexes() {
		val output = engine.template(logger, settings("my.key" to "value"), Document())
		assertEquals(Document("my", Document("key", "value")), output)

		val output2 = engine.template(logger, settings("my.sub.key" to "value"), Document())
		assertEquals(Document("my", Document("sub", Document("key", "value"))), output2)
	}

	@Test
	fun shouldHandleListIndexes() {
		val output = engine.template(logger, settings("my.key[0]" to "value", "my.key[1]" to "value2"), Document())

		assertTrue(output.containsKey("my"))
		val subdoc = output.get("my", Document::class.java)
		assertTrue(subdoc.containsKey("key"))
		val keyList = subdoc.get("key", List::class.java)
		assertEquals(2, keyList.size)
		assertEquals("value", keyList[0])
		assertEquals("value2", keyList[1])

		val output2 = engine.template(logger, settings("my_-key[0]" to "value", "my_-key[1]" to "value2"), Document())
		assertEquals(
			Document("my_-key", listOf("value", "value2")),
			output2
		)

		val output3 = engine.template(logger, settings("key[3]" to "value3", "key[1]" to "value1"), Document())
		assertEquals(
			Document("key", listOf(null, "value1", null, "value3")),
			output3
		)
	}

	@Test
	fun shouldHandleNestedListIndexes() {
		val output = engine.template(logger, settings("my.key[0].first" to "value", "my.key[0].second" to "value2"), Document())
		assertEquals(
			Document("my", Document("key", listOf(Document(mapOf(
				"first" to "value",
				"second" to "value2"
			))))),
			output
		)
	}

	@Test
	fun shouldHandleDoubleNestedListIndexes() {
		val output2 = engine.template(logger, settings("my.key[0].key2[0]" to "value", "my.key[1].key2[1]" to "value2"), Document())

		assertEquals(
			Document("my", Document("key", listOf(Document("key2", listOf("value")), Document("key2", listOf(null, "value2"))))),
			output2
		)
	}

	@Test
	fun shouldNotHandleEscapedListIndexes() {
		val output = engine.template(logger, settings("my.key\\[0]" to "value", "my.key\\[1]" to "value2"), Document())
		assertEquals(
			Document("my", Document("key[0]", "value").append("key[1]", "value2")),
			output
		)

		val output2 = engine.template(logger, settings("my-key\\[0]" to "value", "my-key\\[1]" to "value2"), Document())
		assertEquals(
			Document("my-key[0]", "value").append("my-key[1]", "value2"),
			output2
		)
	}

	@Test
	fun shouldHandleListIndexesWithSpecialCharacters() {
		listOf(
			"my_-key[3]",
			"my_-\\[key[3]",
			"my-----key[3]",
			"my----key[[3]",
			"my-hello[3]",
			"e[3]",
		).forEach {
			val output = engine.template(logger, settings(it to "value"), Document())
			assertEquals(
				Document(
					it.substringBeforeLast("[")
						// escaped characters obviously do not contain the `\` any longer
						.replace("\\", ""),
					listOf(null, null, null, "value")
				),
				output
			)
		}
	}

	class Liquid : MapperEngineTest(MapperEngine.LIQUID) {
		@Test
		override fun simple() {
			val input = Document("og", "value")

			val output = engine.template(logger, settings("my" to "{{ og }}"), input)
			assertEquals(Document("my", "value"), output)
		}

		@Test
		override fun dotToMap() {
			val input = Document("og", "value")

			val output = engine.template(logger, settings("my.key" to "{{ og }}"), input)
			assertEquals(Document("my", Document("key", "value")), output)

			val output2 = engine.template(logger, settings("my.sub.key" to "{{ og }}"), input)
			assertEquals(Document("my", Document("sub", Document("key", "value"))), output2)
		}
	}

	class Replace : MapperEngineTest(MapperEngine.REPLACE) {
		@Test
		override fun simple() {
			val input = Document("og", "value")

			val output = engine.template(logger, settings("my" to "%og%"), input)
			assertEquals(Document("my", "value"), output)
		}

		@Test
		override fun dotToMap() {
			val input = Document("og", "value")

			val output = engine.template(logger, settings("my.key" to "%og%"), input)
			assertEquals(Document("my", Document("key", "value")), output)

			val output2 = engine.template(logger, settings("my.sub.key" to "%og%"), input)
			assertEquals(Document("my", Document("sub", Document("key", "value"))), output2)
		}
	}
}
