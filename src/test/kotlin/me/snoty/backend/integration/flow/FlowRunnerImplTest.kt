package me.snoty.backend.integration.flow

import ch.qos.logback.classic.Logger
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanId
import io.opentelemetry.semconv.ExceptionAttributes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import me.snoty.backend.integration.flow.execution.FlowFeatureFlags
import me.snoty.backend.integration.flow.execution.FlowRunnerImpl
import me.snoty.backend.integration.flow.execution.FlowTracingImpl
import me.snoty.backend.integration.flow.logging.NodeLogAppender
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.observability.JOB_ID
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.test.*
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FlowRunnerImplTest {
	@Serializable
	data class TestNodeSettings(override val name: String) : NodeSettings

	private val json = snotyJson {
		serializersModule += SerializersModule {
			polymorphic(NodeSettings::class) {
				subclass(TestNodeSettings::class, TestNodeSettings.serializer())
				subclass(EmptyNodeSettings::class, EmptyNodeSettings.serializer())
			}
		}
	}

	private val mapHandler = GlobalMapHandler()
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP)), mapHandler)
		registerHandler(nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE)), QuoteHandler)
		registerHandler(nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, TYPE_EXCEPTION)), ExceptionHandler)
		registerEmitHandler()
	}
	private val otel = createOpenTelemetry()
	private val clientAndProvider = testFeatureFlags()
	private val featureFlags = FlowFeatureFlags(clientAndProvider.client)
	private val flagsProvider = clientAndProvider.provider
	private val tracing = FlowTracingImpl(json = json, openTelemetry = otel.openTelemetry, featureFlags = featureFlags)
	private val runner = FlowRunnerImpl(nodeRegistry, featureFlags, IntermediateDataMapperRegistry, tracing, TestFlowExecutionService())
	private val testLogService = TestFlowExecutionService()
	private val logger = (LoggerFactory.getLogger(FlowRunnerImplTest::class.java) as Logger).apply {
		val appender = NodeLogAppender(testLogService)
		addAppender(appender)
		appender.start()
	}

	private suspend fun FlowRunnerImpl.executeStartNode(jobId: String, flow: WorkflowWithNodes, input: IntermediateData)
		= execute(jobId, FlowTriggerReason.Unknown, logger, Level.DEBUG, flow, input)

	private fun assertNoWarnings(flow: Workflow) = runBlocking {
		val output = testLogService.retrieve(flow._id)
			.filter {
				it.level.toInt() >= Level.WARN.toInt()
			}

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
		val node = node(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val emit = emitNode(node)
		val flow = relationalFlow(emit, node)
		val jobId = "basic"
		runner.executeStartNode(jobId, flow, intermediateData)
		assertNoWarnings(flow)
		assertEquals(intermediateDataRaw, mapHandler[node._id])
		val spans = otel.spanExporter.finishedSpanItems
			.sortedBy { it.startEpochNanos }

		assertEquals(3, spans.size)
		assertTrue(spans[0].name.contains(flow._id.toString()))
		assertEquals(tracing.traceName(emit), spans[1].name)
		assertEquals(tracing.traceName(node), spans[2].name)
		assertAny(spans) {
			it.attributes.get(JOB_ID) == jobId
		}
	}

	@Test
	fun `test basic withQuote`(): Unit = runBlocking {
		val map = node(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val processor = node(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE), next = listOf(map))
		val emit = emitNode(processor)
		val flow = relationalFlow(emit, processor, map)

		runner.executeStartNode("basic withQuote", flow, intermediateData)
		assertNoWarnings(flow)
		assertEquals("'test'", mapHandler[map._id])

		val spans = otel.spanExporter.finishedSpanItems
		assertEquals(4, spans.size)
		assertAny(spans) { it.name.contains(tracing.traceName(emit)) }
		assertAny(spans) { it.name.contains(tracing.traceName(processor)) }
		assertAny(spans) { it.name.contains(tracing.traceName(map)) }
	}

	@Test
	fun `test traces config attribute`() = runBlocking {
		val config = TestNodeSettings("test")
		val node = node(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), settings = config)
		val emit = emitNode(node)
		val flow = relationalFlow(emit, node)

		suspend fun verifyTrace(flow: WorkflowWithNodes, input: IntermediateData, withConfig: Boolean) {
			runner.executeStartNode("traces config attribute", flow, input)
			assertNoWarnings(flow)
			assertEquals(input.value, mapHandler[node._id])
			val spans = otel.spanExporter.finishedSpanItems
				.sortedBy { it.startEpochNanos }

			assertEquals(3, spans.size)
			// root node
			assertTrue(spans[0].name.contains(flow._id.toString()))
			assertEquals(tracing.traceName(emit), spans[1].name)
			// execution node (the one with an actual config)
			assertEquals(tracing.traceName(node), spans[2].name)
			val configAttribute = spans[2].attributes.get(AttributeKey.stringKey("config"))
			if (withConfig) {
				assertNotNull(configAttribute)
				assertEquals(configAttribute, json.encodeToString(node.settings))
			} else {
				assertNull(configAttribute)
			}
			otel.spanExporter.reset()
		}

		flagsProvider.setFlagValue(featureFlags::traceConfig, false)
		verifyTrace(flow, intermediateData, withConfig = false)
		flagsProvider.setFlagValue(featureFlags::traceConfig, true)
		verifyTrace(flow, intermediateData, withConfig = true)
	}

	@Test
	fun `test traces exception attributes`() = runBlocking {
		val exNode = node(NodeDescriptor(Subsystem.PROCESSOR, TYPE_EXCEPTION))
		val mapNode = node(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE), next = listOf(exNode))
		val emit = emitNode(mapNode)
		val flow = relationalFlow(emit, mapNode, exNode)

		assertThrows<FlowExecutionException> {
			runner.executeStartNode("traces exception attributes", flow, intermediateData)
		}
		assertNull(mapHandler[exNode._id])
		val spans = otel.spanExporter.finishedSpanItems
		assertEquals(4, spans.size)
		val flowSpan = assertAny(spans) {
			it.name.contains(flow._id.toString())
		}
		assertEquals(SpanId.getInvalid(), flowSpan.parentSpanId)
		assertNull(flowSpan.attributes.get(AttributeKey.stringKey("node.id")))
		assertEquals(flow._id.toString(), flowSpan.attributes.get(AttributeKey.stringKey("flow.id")))

		val emitSpan = assertAny(spans) { it.name.contains(tracing.traceName(emit)) }
		assertEquals(flowSpan.spanId, emitSpan.parentSpanId)

		val mapSpan = assertAny(spans) { it.name.contains(tracing.traceName(mapNode)) }
		assertEquals(emitSpan.spanId, mapSpan.parentSpanId)

		val exceptionSpan = assertAny(spans) { it.name.contains(tracing.traceName(exNode)) }
		assertEquals(mapSpan.spanId, exceptionSpan.parentSpanId)
		assertEquals(1, flowSpan.totalRecordedEvents)
		assertEquals(1, exceptionSpan.totalRecordedEvents)

		val exceptionEvent = exceptionSpan.events[0]
		assertEquals("exception", exceptionEvent.name)
		assertNotNull(exceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertEquals(ExceptionHandler.exception::class.qualifiedName, exceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertNotNull(exceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_MESSAGE))

		val rootExceptionEvent = flowSpan.events[0]
		assertEquals("exception", rootExceptionEvent.name)
		assertNotNull(rootExceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertEquals(FlowExecutionException::class.qualifiedName, rootExceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_TYPE))
		assertNotNull(rootExceptionEvent.attributes.get(ExceptionAttributes.EXCEPTION_MESSAGE))
	}
}
