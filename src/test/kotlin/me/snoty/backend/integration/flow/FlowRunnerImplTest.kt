package me.snoty.backend.integration.flow

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanId
import io.opentelemetry.semconv.ExceptionAttributes
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.observability.JOB_ID
import me.snoty.backend.test.*
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.flow.FlowLogEntry
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FlowRunnerImplTest {
	private val mapHandler = GlobalMapHandler()
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), mapHandler)
		registerHandler(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE), QuoteHandler)
		registerHandler(NodeDescriptor(Subsystem.PROCESSOR, TYPE_EXCEPTION), ExceptionHandler)
	}
	private val tracerExporter = createTestTracer(FlowRunnerImpl::class)
	private val flagsProvider = testFeatureFlags()
	private val runner = FlowRunnerImpl(nodeRegistry, flagsProvider.flags, tracerExporter.tracer)

	private fun FlowRunnerImpl.execute(jobId: String, flow: RelationalFlowNode, input: IntermediateData)
		= execute(jobId, LoggerFactory.getLogger(this::class.java), flow, input)

	private fun assertNoWarnings(output: List<FlowLogEntry>) {
		if (output.isNotEmpty()) {
			println("Warnings:")
			output.forEach {
				println(it.message)
			}
		}
		assertEquals(0, output.size)
	}

	private val intermediateDataRaw = "test"
	private val intermediateData = SimpleIntermediateData(intermediateDataRaw)

	@Test
	fun `test basic`(): Unit = runBlocking {
		val flow = relationalFlow(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val jobId = "basic"
		val output = runner.execute(jobId, flow, intermediateData).toList()
		assertNoWarnings(output)
		assertEquals(intermediateDataRaw, mapHandler[flow._id])
		val spans = tracerExporter.exporter.finishedSpanItems

		assertEquals(2, spans.size)
		assertEquals(traceName(flow), spans[0].name)
		assertTrue(spans[1].name.contains(flow._id.toString()))
		assertAny(spans) {
			it.attributes.get(JOB_ID) == jobId
		}
	}

	@Test
	fun `test basic withQuote`(): Unit = runBlocking {
		val nodeNext = relationalFlow(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val flow = relationalFlow(
			NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE),
			next = listOf(nodeNext)
		)
		val output = runner.execute("basic withQuote", flow, intermediateData).toList()
		// no warnings
		assertNoWarnings(output)
		assertEquals(nodeNext._id, flow.next.first()._id)
		assertEquals("'test'", mapHandler[nodeNext._id])
		val spans = tracerExporter.exporter.finishedSpanItems
		assertEquals(3, spans.size)
		assertAny(spans) { it.name.contains(traceName(flow)) }
		assertAny(spans) { it.name.contains(traceName(nodeNext)) }
	}

	@Test
	fun `test traces config attribute`() = runBlocking {
		val config = Document("key", "value")
		val flow = relationalFlow(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), config = config)

		suspend fun verifyTrace(flow: RelationalFlowNode, input: IntermediateData, withConfig: Boolean) {
			val output = runner.execute("traces config attribute", flow, input).toList()
			assertNoWarnings(output)
			assertEquals(input.value, mapHandler[flow._id])
			val spans = tracerExporter.exporter.finishedSpanItems

			assertEquals(2, spans.size)
			// root node
			assertTrue(spans[1].name.contains(flow._id.toString()))
			// execution node (the one with an actual config)
			assertEquals(traceName(flow), spans[0].name)
			val configAttribute = spans[0].attributes.get(AttributeKey.stringKey("config"))
			if (withConfig) {
				assertNotNull(configAttribute)
				assertEquals(configAttribute, flow.config.toJson())
			} else {
				assertNull(configAttribute)
			}
			tracerExporter.exporter.reset()
		}

		flagsProvider.provider.setFlagValue(flagsProvider.flags.flow_traceConfig, false)
		verifyTrace(flow, intermediateData, withConfig = false)
		flagsProvider.provider.setFlagValue(flagsProvider.flags.flow_traceConfig, true)
		verifyTrace(flow, intermediateData, withConfig = true)
	}

	@Test
	fun `test traces exception attributes`() = runBlocking {
		val nextNode = relationalFlow(NodeDescriptor(Subsystem.PROCESSOR, TYPE_EXCEPTION))
		val startNode = relationalFlow(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE), next = listOf(nextNode))

		try {
			runner.execute("traces exception attributes", startNode, intermediateData)
				.collect()
		} catch (e: FlowExecutionException) {
			// expected
			assertNull(mapHandler[nextNode._id])
		}
		val spans = tracerExporter.exporter.finishedSpanItems
		assertEquals(3, spans.size)
		val flowSpan = assertAny(spans) { it.name.contains(traceName(startNode)) }
		val exceptionSpan = assertAny(spans) { it.name.contains(traceName(nextNode)) }
		assertEquals(flowSpan.spanId, exceptionSpan.parentSpanId)
		// start node has no exceptions
		assertEquals(0, flowSpan.totalRecordedEvents)
		assertEquals(1, exceptionSpan.totalRecordedEvents)
		val exceptionEvent = exceptionSpan.events[0]
		assertEquals("exception", exceptionEvent.name)
		assertNotNull(exceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertEquals(ExceptionHandler.exception::class.qualifiedName, exceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertNotNull(exceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_MESSAGE))
		val rootSpan = assertAny(spans) {
			// "invalid" => there's no parent
			it.parentSpanId == SpanId.getInvalid()
				&& it.attributes.get(AttributeKey.stringKey("node.id")) == startNode._id.toString()
		}
		val rootExceptionEvent = rootSpan.events[0]
		assertEquals(rootSpan.spanId, flowSpan.parentSpanId)
		assertEquals("exception", rootExceptionEvent.name)
		assertNotNull(rootExceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertEquals(FlowExecutionException::class.qualifiedName, rootExceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertNotNull(rootExceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_MESSAGE))
	}
}
