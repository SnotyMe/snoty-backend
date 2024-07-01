package me.snoty.backend.integration.flow

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.test.GlobalMapHandler
import me.snoty.backend.test.QuoteHandler
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.flow.FlowOutput
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import java.util.*

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

	private fun FlowRunnerImpl.execute(flow: RelationalFlowNode, input: String)
		= execute(LoggerFactory.getLogger(this::class.java), flow, input)

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
	fun testRun_basic() = runBlocking {
		val flow = RelationalFlowNode(
			NodeId(),
			UUID.randomUUID(),
			NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP),
			Document(),
			listOf()
		)
		val input = "test"
		val output = runner.execute(flow, input).toList()
		assertNoWarnings(output)
		assertEquals(input, mapHandler[flow._id])
	}

	@Test
	fun testRun_basic_withQuote() = runBlocking {
		val flow = RelationalFlowNode(
			NodeId(),
			UUID.randomUUID(),
			NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE),
			Document(),
			listOf(
				RelationalFlowNode(
					NodeId(),
					UUID.randomUUID(),
					NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP),
					Document(),
					listOf()
				)
			)
		)
		val input = "test"
		val output = runner.execute(flow, input).toList()
		// no warnings
		assertNoWarnings(output)
		assertEquals("'$input'", mapHandler[flow.next.first()._id])
	}
}
