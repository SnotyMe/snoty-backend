package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.NodeDescriptor
import me.snoty.backend.integration.flow.model.Subsystem
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.test.GlobalMapHandler
import me.snoty.backend.test.QuoteHandler
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private const val TYPE_MAP = "map"
private const val TYPE_QUOTE = "quote"

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FlowRunnerImplTest {
	private val mapHandler = GlobalMapHandler()
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), mapHandler)
		registerHandler(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE), QuoteHandler)
	}
	private val runner = FlowRunnerImpl(nodeRegistry)

	private fun assertNoWarnings(output: List<FlowOutput>) {
		if (output.isNotEmpty()) {
			println("Warnings:")
			output.forEach {
				println(it.message)
			}
		}
		assertEquals(0, output.size)
	}

	@Test
	fun testRun_basic() {
		val flow = FlowNode(
			NodeId(),
			NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP),
			Document(),
			listOf()
		)
		val input = "test"
		val output = runner.execute(listOf(flow), input)
		assertNoWarnings(output)
		assertEquals(input, mapHandler[flow.id])
	}

	@Test
	fun testRun_basic_withQuote() {
		val flow = FlowNode(
			NodeId(),
			NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE),
			Document(),
			listOf(FlowNode(
				NodeId(),
				NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP),
				Document(),
				listOf()
			))
		)
		val input = "test"
		val output = runner.execute(listOf(flow), input)
		// no warnings
		assertNoWarnings(output)
		assertEquals("'$input'", mapHandler[flow.next.first().id])
	}
}
