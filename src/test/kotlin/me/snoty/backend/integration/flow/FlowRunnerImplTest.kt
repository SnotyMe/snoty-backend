package me.snoty.backend.integration.flow

import ch.qos.logback.classic.Logger
import io.mockk.mockk
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanId
import io.opentelemetry.semconv.ExceptionAttributes
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import me.snoty.backend.integration.flow.execution.FlowRunnerImpl
import me.snoty.backend.integration.flow.execution.FlowTracingImpl
import me.snoty.backend.integration.flow.logging.NodeLogAppender
import me.snoty.backend.logging.KMDC
import me.snoty.backend.observability.JOB_ID
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.test.*
import me.snoty.backend.utils.snotyJson
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.impl.SimpleIntermediateData
import me.snoty.backend.wiring.flow.FlowFeatureFlags
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.node.EmptyNodeSettings
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.registry.NodeRegistryImpl
import me.snoty.core.flow.WorkflowWithNodes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.Koin
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FlowRunnerImplTest {

	@Serializable
	data object TestNodeSettings : NodeSettings

	private val json = snotyJson {
		serializersModule += SerializersModule {
			polymorphic(NodeSettings::class) {
				subclass(TestNodeSettings::class, TestNodeSettings.serializer())
				subclass(EmptyNodeSettings::class, EmptyNodeSettings.serializer())
			}
		}
	}

	private val mapHandler = GlobalMapHandler()
	private val wantsEmptyProvidesNonEmptyHandler = WantsEmptyProvidesNonEmptyHandler()
	private val wantsNonEmptyProvidesEmptyHandler = WantsNonEmptyProvidesEmptyHandler()
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(name = TYPE_MAP, receiveEmptyInput = false), mapHandler)
		registerHandler(nodeMetadata(name = TYPE_QUOTE, receiveEmptyInput = false), QuoteHandler)
		registerHandler(nodeMetadata(name = TYPE_EXCEPTION, receiveEmptyInput = false), ExceptionHandler)
		registerHandler(nodeMetadata(name = TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY, receiveEmptyInput = true), wantsEmptyProvidesNonEmptyHandler)
		registerHandler(nodeMetadata(name = TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY, receiveEmptyInput = false), wantsNonEmptyProvidesEmptyHandler)
		registerEmitHandler()
	}
	private val otel = createOpenTelemetry()
	private val clientAndProvider = testFeatureFlags()
	private val featureFlags = FlowFeatureFlags(clientAndProvider.client)
	private val flagsProvider = clientAndProvider.provider
	private val tracing = FlowTracingImpl(json = json, openTelemetry = otel.openTelemetry, featureFlags = featureFlags)
	private val testFlowExecutionService = TestFlowExecutionService()
	private val testFlowExecutionEventService: FlowExecutionEventService = mockk(relaxed = true)
	private val runner = FlowRunnerImpl(
		Koin(),
		nodeRegistry,
		TestCredentialService,
		IntermediateDataMapperRegistry,
		tracing,
		testFlowExecutionService,
		testFlowExecutionEventService,
		mockk(relaxed = true),
	)
	private val logger = (LoggerFactory.getLogger(FlowRunnerImplTest::class.java) as Logger).apply {
		val appender = NodeLogAppender(testFlowExecutionService, testFlowExecutionEventService)
		addAppender(appender)
		appender.start()
	}

	private suspend fun FlowRunnerImpl.executeStartNode(jobId: String, flow: WorkflowWithNodes, input: NodeInput) {
		// set in the scheduler
		KMDC.put(JOB_ID, jobId)
		withContext(MDCContext()) {
			execute(jobId, FlowTriggerReason.Unknown, logger, Level.DEBUG, flow, input)
		}
	}

	private val intermediateDataRaw = "test"
	private val intermediateData = SimpleIntermediateData(intermediateDataRaw)

	@Test
	fun `test basic`(): Unit = runBlocking {
		val node = node(TYPE_MAP)
		val emit = emitNode(node)
		val flow = relationalFlow(emit, node)
		val jobId = "basic"
		runner.executeStartNode(jobId, flow, listOf(intermediateData))
		assertEquals(listOf(intermediateData), mapHandler[node.id])
		val spans = otel.spanExporter.finishedSpanItems
			.sortedBy { it.startEpochNanos }

		assertEquals(3, spans.size)
		assertTrue(spans[0].name.contains(flow.id.value))
		assertEquals(tracing.traceName(emit), spans[1].name)
		assertEquals(tracing.traceName(node), spans[2].name)
		assertAny(spans) {
			it.attributes.get(JOB_ID) == jobId
		}
	}

	@Test
	fun `test basic withQuote`(): Unit = runBlocking {
		val map = node(TYPE_MAP)
		val processor = node(TYPE_QUOTE, next = listOf(map))
		val emit = emitNode(processor)
		val flow = relationalFlow(emit, processor, map)

		runner.executeStartNode("basic withQuote", flow, listOf(intermediateData))
		assertEquals("'test'", mapHandler[map.id]?.single()?.value)

		val spans = otel.spanExporter.finishedSpanItems
		assertEquals(4, spans.size)
		assertAny(spans) { it.name.contains(tracing.traceName(emit)) }
		assertAny(spans) { it.name.contains(tracing.traceName(processor)) }
		assertAny(spans) { it.name.contains(tracing.traceName(map)) }
	}

	@Test
	fun `test traces config attribute`() = runBlocking {
		val config = TestNodeSettings
		val node = node(TYPE_MAP, settings = config)
		val emit = emitNode(node)
		val flow = relationalFlow(emit, node)

		suspend fun verifyTrace(flow: WorkflowWithNodes, input: IntermediateData, withConfig: Boolean) {
			runner.executeStartNode("traces config attribute", flow, listOf(input))
			assertEquals(input, mapHandler[node.id]?.single())
			val spans = otel.spanExporter.finishedSpanItems
				.sortedBy { it.startEpochNanos }

			assertEquals(3, spans.size)
			// root node
			assertTrue(spans[0].name.contains(flow.id.value))
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
		val exNode = node(TYPE_EXCEPTION)
		val mapNode = node(TYPE_QUOTE, next = listOf(exNode))
		val emit = emitNode(mapNode)
		val flow = relationalFlow(emit, mapNode, exNode)

		assertThrows<FlowExecutionException> {
			runner.executeStartNode("traces exception attributes", flow, listOf(intermediateData))
		}
		assertNull(mapHandler[exNode.id])
		val spans = otel.spanExporter.finishedSpanItems
		assertEquals(4, spans.size)
		val flowSpan = assertAny(spans) {
			it.name.contains(flow.id.value)
		}
		assertEquals(SpanId.getInvalid(), flowSpan.parentSpanId)
		assertNull(flowSpan.attributes.get(AttributeKey.stringKey("node.id")))
		assertEquals(flow.id.value, flowSpan.attributes.get(AttributeKey.stringKey("flow.id")))

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
	
	@Test
	fun `test receive empty input transitively - #225`() = runBlocking {
		val nodex = node(TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY)
		val nodex1 = node(TYPE_QUOTE, next = listOf(nodex))
		val nodex2 = node(TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY, next = listOf(nodex1))
		val nodex3 = node(TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY, next = listOf(nodex2)) // also skipped
		val nodex4 = node(TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY, next = listOf(nodex3)) // skipped
		val emit = emitNode(nodex4) // won't emit anything

		val flow = relationalFlow(emit, nodex4, nodex3, nodex2, nodex1, nodex)
		println(flow.nodes.joinToString("\n") { "${it.id}: ${it.type.value}" })

		runner.executeStartNode("receive empty input transitively", flow, emptyList())

		assertFalse(nodex4.id in wantsNonEmptyProvidesEmptyHandler)
		assertFalse(nodex3.id in wantsNonEmptyProvidesEmptyHandler)
		assertEquals(emptyList<SimpleIntermediateData>(), wantsEmptyProvidesNonEmptyHandler[nodex2.id])
		assertEquals(
			listOf(SimpleIntermediateData("'${WantsEmptyProvidesNonEmptyHandler.OUTPUT}'")),
			wantsEmptyProvidesNonEmptyHandler[nodex.id]
		)
	}
}
