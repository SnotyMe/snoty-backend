package me.snoty.backend.wiring.flow

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.test.TestIds.USER_ID_1
import me.snoty.backend.test.TestIds.USER_ID_CONTROL
import me.snoty.backend.test.assertAny
import me.snoty.backend.test.nodeMetadata
import me.snoty.core.FlowId
import me.snoty.core.Node
import me.snoty.core.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

abstract class FlowServiceSpec(private val makeId: () -> FlowId) {
	protected abstract val service: FlowService
	protected abstract val nodeService: NodeService

	protected val flowScheduler: FlowScheduler = mockk(relaxed = true)
	protected val nodeRegistry: NodeRegistry = mockk(relaxed = true)

	init {
		val descriptorSlot = slot<NodeDescriptor>()
		every { nodeRegistry.getMetadata(capture(descriptorSlot)) } answers {
			val descriptor = descriptorSlot.captured
			nodeMetadata(name = descriptor.name, settingsClass = EmptyNodeSettings::class)
		}
	}

	data class FlowTestContext(
		val userId: UserId,
		val flow: Workflow,
		val flowId: FlowId,
	)

	private fun test(block: suspend FlowTestContext.() -> Unit) = runBlocking {
		val userId = USER_ID_1
		val flow = service.create(userId, "test", WorkflowSettings())
		block(FlowTestContext(userId = userId, flow = flow, flowId = flow.id))
	}

	private fun List<FlowNode>.assertContains(node: StandaloneNode): FlowNode =
		assertAny(this) {
			it.id == node.id
		}

	@Test
	fun testCreateFlow() = test {
		val result = service.getWithNodes(USER_ID_1, flowId)
		assertNotNull(result)
		assertEquals(0, result.nodes.size)

		verify(exactly = 1) {
			flowScheduler.schedule(match { it.id == result.id })
		}
	}

	@Test
	fun testEmptyFlow() = test {
		val result = service.getWithNodes(userId, flowId)
		assertNotNull(result)
		assertEquals(0, result.nodes.size)
	}

	@Test
	fun testNonExistentFlow() = test {
		val result = service.getWithNodes(userId, makeId())
		assertEquals(null, result)
	}

	@Test
	fun testFlowAccessControl() = test {
		assertNotNull(service.getWithNodes(userId, flowId))
		assertNotNull(service.getWithNodes(null, flowId))
		assertNull(service.getWithNodes(USER_ID_CONTROL, flowId))
	}

	private suspend fun FlowTestContext.node(name: String, vararg next: Node): StandaloneNode {
		val newNode = nodeService.create(
			userId = userId,
			flow = flow,
			descriptor = NodeDescriptor(javaClass.packageName, name),
			position = NodePosition(0, 0, 300, 200),
			settings = EmptyNodeSettings(name)
		)

		nodeRegistry.registerHandler(NodeMetadata(
			descriptor = newNode.descriptor,
			displayName = name,
			settingsClass = EmptyNodeSettings::class,
			position = mockk(),
			settings = mockk(),
			input = mockk(),
			output = mockk(),
		), mockk(relaxed = true))

		next.forEach { nodeService.connect(newNode, it) }

		return newNode
	}

	@Test
	fun testDirectFlow() = test {
		val target = node("target")
		val source = node("source", target)

		val result = service.getWithNodes(userId, flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(2, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source.id, sourceNode.id)
		assertEquals(1, sourceNode.next.size)

		val targetNode = nodes.assertContains(target)
		assertEquals(target.id, targetNode.id)
		assertEquals(0, targetNode.next.size)
	}

	@Test
	fun testDirectFlow_twoTargets() = test {
		val target1 = node("target1")
		val target2 = node("target2")
		val source = node("source", target1, target2)

		val result = service.getWithNodes(userId, flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(3, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source.id, sourceNode.id)

		val targetNode1 = nodes.assertContains(target1)
		assertEquals(target1.id, targetNode1.id)

		val targetNode2 = nodes.assertContains(target2)
		assertEquals(target2.id, targetNode2.id)
		assertEquals(0, targetNode1.next.size)
		assertEquals(0, targetNode2.next.size)
	}

	@Test
	fun testIndirectFlow() = test {
		val target = node("target")
		val mapper = node("mapper", target)
		val source = node("start", mapper)

		val result = service.getWithNodes(userId, flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(3, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source.id, sourceNode.id)
		assertEquals(1, sourceNode.next.size)
		assertEquals(mapper.id, sourceNode.next[0])

		val mapperNode = nodes.assertContains(mapper)
		assertEquals(mapper.id, mapperNode.id)
		assertEquals(1, mapperNode.next.size)
		assertEquals(target.id, mapperNode.next[0])

		val targetNode = nodes.assertContains(target)
		assertEquals(target.id, targetNode.id)
		assertEquals(0, targetNode.next.size)
	}

	@Test
	fun testIndirectFlow_twoTargets() = test {
		val target1 = node("target1")
		val target2 = node("target2")
		val mapper = node("mapper", target1, target2)
		val source = node("source", mapper)
		val result = service.getWithNodes(userId, flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(4, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source.id, sourceNode.id)
		assertEquals(1, sourceNode.next.size)
		assertEquals(mapper.id, sourceNode.next[0])

		val mapperNode = nodes.assertContains(mapper)
		assertEquals(mapper.id, mapperNode.id)
		assertEquals(2, mapperNode.next.size)
		assertEquals(target1.id, mapperNode.next[0])
		assertEquals(target2.id, mapperNode.next[1])

		val targetNode1 = nodes.assertContains(target1)
		assertEquals(target1.id, targetNode1.id)
		assertEquals(0, targetNode1.next.size)

		val targetNode2 = nodes.assertContains(target2)
		assertEquals(target2.id, targetNode2.id)
		assertEquals(0, targetNode2.next.size)
	}

	@Test
	fun testIndirectFlow_recursion() = test {
		val mapper1 = node("mapper1")
		val mapper2 = node("mapper2")
		nodeService.connect(mapper1, mapper2)
		nodeService.connect(mapper2, mapper1)
		val source = node("source", mapper1)

		val result = service.getWithNodes(userId, flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(3, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source.id, sourceNode.id)
		assertEquals(1, sourceNode.next.size)
		assertEquals(mapper1.id, sourceNode.next[0])

		val mapperNode1 = nodes.assertContains(mapper1)
		assertEquals(mapper1.id, mapperNode1.id)
		assertEquals(1, mapperNode1.next.size)
		assertEquals(mapper2.id, mapperNode1.next[0])

		val mapperNode2 = nodes.assertContains(mapper2)
		assertEquals(mapper2.id, mapperNode2.id)
		assertEquals(1, mapperNode2.next.size)
		assertEquals(mapper1.id, mapperNode2.next[0])
	}
}
