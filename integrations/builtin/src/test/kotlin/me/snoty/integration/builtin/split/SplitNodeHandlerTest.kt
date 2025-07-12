package me.snoty.integration.builtin.split

import io.mockk.mockk
import me.snoty.backend.test.node
import me.snoty.backend.test.process
import me.snoty.integration.common.runNodeHandlerTest
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import org.bson.Document
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SplitNodeHandlerTest {
	@Test
	fun `replace root`() = runNodeHandlerTest(SplitNodeHandler()) { `replace root impl`() }

	context(context: NodeHandleContext, handler: SplitNodeHandler)
	private suspend fun `replace root impl`() {
		val node = node(mockk(), settings = SplitSettings(key = "key", behavior = SplitBehavior.REPLACE_ROOT))
		val input = listOf(
			mapOf("key" to listOf(Document("value1", true), "value2"), "other" to "sharedValue")
		).map { context.intermediateDataMapperRegistry[BsonIntermediateData::class].serialize(it) }

		val output = process(node, input)

		val expectedOutput = listOf(
			context.intermediateDataMapperRegistry[BsonIntermediateData::class].serialize(Document("value1", true)),
			context.intermediateDataMapperRegistry[SimpleIntermediateData::class].serialize("value2"),
		)

		assertEquals(expectedOutput, output)
	}

	@Test
	fun `replace key`() = runNodeHandlerTest(SplitNodeHandler()) { `replace key impl`() }

	context(context: NodeHandleContext, handler: SplitNodeHandler)
	private suspend fun `replace key impl`() {
		val node = node(mockk(), settings = SplitSettings(key = "key", behavior = SplitBehavior.REPLACE_KEY))
		val input = listOf(
			mapOf("key" to listOf(Document("value1", true), "value2"), "other" to "sharedValue")
		).map { context.intermediateDataMapperRegistry[BsonIntermediateData::class].serialize(it) }

		val output = process(node, input)

		val expectedOutput = listOf(
			mapOf("key" to Document("value1", true), "other" to "sharedValue"),
			mapOf("key" to "value2", "other" to "sharedValue")
		).map { context.intermediateDataMapperRegistry[BsonIntermediateData::class].serialize(it) }

		assertEquals(expectedOutput, output)
	}
}
