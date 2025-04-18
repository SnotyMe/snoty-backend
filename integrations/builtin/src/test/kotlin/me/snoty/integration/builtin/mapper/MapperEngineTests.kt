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
	fun map() {
		val output = engine.template(logger, settings("my.key" to "value"), Document())
		assertEquals(Document("my", Document("key", "value")), output)
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
		assertTrue(output2.containsKey("key"))
		val keyList2 = output2.get("my_-key", List::class.java)
		assertEquals(2, keyList2.size)
		assertEquals("value", keyList2[0])
		assertEquals("value2", keyList2[1])

		val output3 = engine.template(logger, settings("key[3]" to "value3", "key[1]" to "value1"), Document())
		assertTrue(output3.containsKey("key"))
		val keyList3 = output3.get("key", List::class.java)
		assertEquals("value3", keyList3[3])
		assertEquals("value1", keyList3[1])
	}

	@Test
	fun shouldNotHandleEscapedListIndexes() {
		val output = engine.template(logger, settings("my.key\\[0]" to "value", "my.key\\[1]" to "value2"), Document())
		assertTrue(output.containsKey("my"))
		val subdoc = output.get("my", Document::class.java)
		assertEquals("value", subdoc["key\\[0]"])
		assertEquals("value2", subdoc["key\\[1]"])
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
