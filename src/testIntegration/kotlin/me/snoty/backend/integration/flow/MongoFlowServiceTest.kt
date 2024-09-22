package me.snoty.backend.integration.flow

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.assertAny
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.GenericNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNotNull

class MongoFlowServiceTest {
	private val userId = UUID.randomUUID()

	private val mongoDB = MongoTest.getMongoDatabase {}
	private val nodeRegistry = NodeRegistryImpl()
	private val nodeService: MongoNodeService = MongoNodeService(mongoDB, nodeRegistry, mockk(relaxed = true))
	private val flowScheduler: FlowScheduler = mockk(relaxed = true)
	private val service = MongoFlowService(mongoDB, flowScheduler, mockk(relaxed = true))

	data class FlowTestContext(
		val flowId: NodeId = NodeId(),
	)

	private fun test(block: suspend FlowTestContext.() -> Unit) = runBlocking {
		val flow = service.create(userId, "test")
		block(FlowTestContext(flowId = flow._id))
	}

	private fun List<FlowNode>.assertContains(node: StandaloneNode): FlowNode =
		assertAny(this) {
			it._id == node._id
		}

	@Test
	fun testCreateFlow() = test {
		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		assertEquals(0, result.nodes.size)

		verify(exactly = 1) {
			flowScheduler.schedule(match { it._id == result._id })
		}
	}

	@Test
	fun testEmptyFlow() = test {
		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		assertEquals(0, result.nodes.size)
	}

	@Test
	fun testNonExistentFlow() = test {
		val result = service.getWithNodes(NodeId())
		assertEquals(null, result)
	}

	private suspend fun FlowTestContext.node(name: String, vararg next: GenericNode): StandaloneNode {
		val newNode = nodeService.create(
			userID = userId,
			flowId = flowId,
			descriptor = NodeDescriptor(Subsystem.INTEGRATION, name),
			settings = EmptyNodeSettings(name)
		)

		next.forEach { nodeService.connect(newNode._id, it._id) }

		return newNode
	}

	@Test
	fun testDirectFlow() = test {
		val target = node("target")
		val source = node("source", target)

		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(2, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source._id, sourceNode._id)
		assertEquals(1, sourceNode.next.size)

		val targetNode = nodes.assertContains(target)
		assertEquals(target._id, targetNode._id)
		assertEquals(0, targetNode.next.size)
	}

	@Test
	fun testDirectFlow_twoTargets() = test {
		val target1 = node("target1")
		val target2 = node("target2")
		val source = node("source", target1, target2)

		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(3, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source._id, sourceNode._id)

		val targetNode1 = nodes.assertContains(target1)
		assertEquals(target1._id, targetNode1._id)

		val targetNode2 = nodes.assertContains(target2)
		assertEquals(target2._id, targetNode2._id)
		assertEquals(0, targetNode1.next.size)
		assertEquals(0, targetNode2.next.size)
	}

	@Test
	fun testIndirectFlow() = test {
		val target = node("target")
		val mapper = node("mapper", target)
		val source = node("start", mapper)

		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(3, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source._id, sourceNode._id)
		assertEquals(1, sourceNode.next.size)
		assertEquals(mapper._id, sourceNode.next[0])

		val mapperNode = nodes.assertContains(mapper)
		assertEquals(mapper._id, mapperNode._id)
		assertEquals(1, mapperNode.next.size)
		assertEquals(target._id, mapperNode.next[0])

		val targetNode = nodes.assertContains(target)
		assertEquals(target._id, targetNode._id)
		assertEquals(0, targetNode.next.size)
	}

	@Test
	fun testIndirectFlow_twoTargets() = test {
		val target1 = node("target1")
		val target2 = node("target2")
		val mapper = node("mapper", target1, target2)
		val source = node("source", mapper)
		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(4, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source._id, sourceNode._id)
		assertEquals(1, sourceNode.next.size)
		assertEquals(mapper._id, sourceNode.next[0])

		val mapperNode = nodes.assertContains(mapper)
		assertEquals(mapper._id, mapperNode._id)
		assertEquals(2, mapperNode.next.size)
		assertEquals(target1._id, mapperNode.next[0])
		assertEquals(target2._id, mapperNode.next[1])

		val targetNode1 = nodes.assertContains(target1)
		assertEquals(target1._id, targetNode1._id)
		assertEquals(0, targetNode1.next.size)

		val targetNode2 = nodes.assertContains(target2)
		assertEquals(target2._id, targetNode2._id)
		assertEquals(0, targetNode2.next.size)
	}

	@Test
	fun testIndirectFlow_recursion() = test {
		val mapper1 = node("mapper1")
		val mapper2 = node("mapper2")
		nodeService.connect(mapper1._id, mapper2._id)
		nodeService.connect(mapper2._id, mapper1._id)
		val source = node("source", mapper1)

		val result = service.getWithNodes(flowId)
		assertNotNull(result)
		val nodes = result.nodes
		assertEquals(3, nodes.size)

		val sourceNode = nodes.assertContains(source)
		assertEquals(source._id, sourceNode._id)
		assertEquals(1, sourceNode.next.size)
		assertEquals(mapper1._id, sourceNode.next[0])

		val mapperNode1 = nodes.assertContains(mapper1)
		assertEquals(mapper1._id, mapperNode1._id)
		assertEquals(1, mapperNode1.next.size)
		assertEquals(mapper2._id, mapperNode1.next[0])

		val mapperNode2 = nodes.assertContains(mapper2)
		assertEquals(mapper2._id, mapperNode2._id)
		assertEquals(1, mapperNode2.next.size)
		assertEquals(mapper1._id, mapperNode2.next[0])
	}
}
