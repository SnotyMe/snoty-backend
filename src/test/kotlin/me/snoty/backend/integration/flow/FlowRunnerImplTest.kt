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
import me.snoty.backend.wiring.flow.FlowFeatureFlags
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.node.NodeRegistryImpl
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.Koin
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FlowRunnerImplTest {
	private val namespace = javaClass.packageName

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

	fun nodeMetadata(name: String, receiveEmptyInput: Boolean) = nodeMetadata(namespace = namespace, name = name, receiveEmptyInput = receiveEmptyInput)

	private val mapHandler = GlobalMapHandler()
	private val wantsEmptyProvidesNonEmptyHandler = WantsEmptyProvidesNonEmptyHandler()
	private val wantsNonEmptyProvidesEmptyHandler = WantsNonEmptyProvidesEmptyHandler()
	private val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(name = TYPE_MAP, false), mapHandler)
		registerHandler(nodeMetadata(name = TYPE_QUOTE, false), QuoteHandler)
		registerHandler(nodeMetadata(name = TYPE_EXCEPTION, false), ExceptionHandler)
		registerHandler(nodeMetadata(name = TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY, true), wantsEmptyProvidesNonEmptyHandler)
		registerHandler(nodeMetadata(name = TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY, false), wantsNonEmptyProvidesEmptyHandler)
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

	private fun assertNoWarnings(flow: Workflow) = runBlocking {
		val output = testFlowExecutionService.retrieve(flow._id)
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
		val node = node(NodeDescriptor(namespace, TYPE_MAP))
		val emit = emitNode(node)
		val flow = relationalFlow(emit, node)
		val jobId = "basic"
		runner.executeStartNode(jobId, flow, listOf(intermediateData))
		assertNoWarnings(flow)
		assertEquals(listOf(intermediateData), mapHandler[node._id])
		val spans = otel.spanExporter.finishedSpanItems
			.sortedBy { it.startEpochNanos }

		assertEquals(3, spans.size)
		assertTrue(spans[0].name.contains(flow._id))
		assertEquals(tracing.traceName(emit), spans[1].name)
		assertEquals(tracing.traceName(node), spans[2].name)
		assertAny(spans) {
			it.attributes.get(JOB_ID) == jobId
		}
	}

	@Test
	fun `test basic withQuote`(): Unit = runBlocking {
		val map = node(NodeDescriptor(namespace, TYPE_MAP))
		val processor = node(NodeDescriptor(namespace, TYPE_QUOTE), next = listOf(map))
		val emit = emitNode(processor)
		val flow = relationalFlow(emit, processor, map)

		runner.executeStartNode("basic withQuote", flow, listOf(intermediateData))
		assertNoWarnings(flow)
		assertEquals("'test'", mapHandler[map._id]?.single()?.value)

		val spans = otel.spanExporter.finishedSpanItems
		assertEquals(4, spans.size)
		assertAny(spans) { it.name.contains(tracing.traceName(emit)) }
		assertAny(spans) { it.name.contains(tracing.traceName(processor)) }
		assertAny(spans) { it.name.contains(tracing.traceName(map)) }
	}

	@Test
	fun `test traces config attribute`() = runBlocking {
		val config = TestNodeSettings("test")
		val node = node(NodeDescriptor(namespace, TYPE_MAP), settings = config)
		val emit = emitNode(node)
		val flow = relationalFlow(emit, node)

		suspend fun verifyTrace(flow: WorkflowWithNodes, input: IntermediateData, withConfig: Boolean) {
			runner.executeStartNode("traces config attribute", flow, listOf(input))
			assertNoWarnings(flow)
			assertEquals(input, mapHandler[node._id]?.single())
			val spans = otel.spanExporter.finishedSpanItems
				.sortedBy { it.startEpochNanos }

			assertEquals(3, spans.size)
			// root node
			assertTrue(spans[0].name.contains(flow._id))
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
		val exNode = node(NodeDescriptor(namespace, TYPE_EXCEPTION))
		val mapNode = node(NodeDescriptor(namespace, TYPE_QUOTE), next = listOf(exNode))
		val emit = emitNode(mapNode)
		val flow = relationalFlow(emit, mapNode, exNode)

		assertThrows<FlowExecutionException> {
			runner.executeStartNode("traces exception attributes", flow, listOf(intermediateData))
		}
		assertNull(mapHandler[exNode._id])
		val spans = otel.spanExporter.finishedSpanItems
		assertEquals(4, spans.size)
		val flowSpan = assertAny(spans) {
			it.name.contains(flow._id)
		}
		assertEquals(SpanId.getInvalid(), flowSpan.parentSpanId)
		assertNull(flowSpan.attributes.get(AttributeKey.stringKey("node.id")))
		assertEquals(flow._id, flowSpan.attributes.get(AttributeKey.stringKey("flow.id")))

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
		val nodex = node(NodeDescriptor(namespace, TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY))
		val nodex1 = node(NodeDescriptor(namespace, TYPE_QUOTE), next = listOf(nodex))
		val nodex2 = node(NodeDescriptor(namespace, TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY), next = listOf(nodex1))
		val nodex3 = node(NodeDescriptor(namespace, TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY), next = listOf(nodex2)) // also skipped
		val nodex4 = node(NodeDescriptor(namespace, TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY), next = listOf(nodex3)) // skipped
		val emit = emitNode(nodex4) // won't emit anything

		val flow = relationalFlow(emit, nodex4, nodex3, nodex2, nodex1, nodex)
		println(flow.nodes.joinToString("\n") { "${it._id}: ${it.descriptor.name}" })

		runner.executeStartNode("receive empty input transitively", flow, emptyList())

		assertNoWarnings(flow)
		assertFalse(nodex4._id in wantsNonEmptyProvidesEmptyHandler)
		assertFalse(nodex3._id in wantsNonEmptyProvidesEmptyHandler)
		assertEquals(emptyList<SimpleIntermediateData>(), wantsEmptyProvidesNonEmptyHandler[nodex2._id])
		assertEquals(
			listOf(SimpleIntermediateData("'${WantsEmptyProvidesNonEmptyHandler.OUTPUT}'")),
			wantsEmptyProvidesNonEmptyHandler[nodex._id]
		)
	}
}
