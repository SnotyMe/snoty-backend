package me.snoty.integration.builtin.mapper

import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

abstract class MapperEngineTest(val engine: MapperEngine) {
	val logger = LoggerFactory.getLogger(MapperEngineTest::class.java)!!

	protected fun settings(vararg keys: Pair<String, String>) = MapperSettings(engine = engine, fields = keys.toMap())

	abstract fun simple()
	abstract fun dotToMap()

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
