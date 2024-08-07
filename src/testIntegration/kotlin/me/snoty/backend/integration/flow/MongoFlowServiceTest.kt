package me.snoty.backend.integration.flow

import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.backend.test.MongoTest
import me.snoty.backend.test.TestFlowBuilder
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class MongoFlowServiceTest : AbstractFlowFetchTest<MongoFlowServiceTest.FlowTestContextImpl>(::FlowTestContextImpl) {
	data class FlowTestContextImpl(
		override var flow: List<RelationalFlowNode>? = null
	) : FlowTestContext

	private val USER_ID = UUID.randomUUID()

	private val mongoDB = MongoTest.getMongoDatabase {}
	private val service = object : MongoFlowService(mongoDB, TestFlowBuilder, mockk()) {
		context(FlowTestContext)
		fun getFlowForNode_test(node: GraphNode) = runBlocking {
			val flow = getFlowForNode(StandaloneNode(node._id, USER_ID, node.descriptor, EmptyNodeSettings())).toList()
			this@FlowTestContext.flow = flow
			flow
		}

		// test-only method, you'd usually use a `NodeService` for this
		fun insertNode(node: GraphNode) = runBlocking {
			collection.insertOne(node)
		}
	}

	private fun sourceNode() = graphNode("source")
	private fun sourceNode(vararg next: GraphNode)
		= graphNode("source", *next)
	private fun sourceNode(vararg next: NodeId) = graphNode("source", *next)

	private fun graphNode(type: String, vararg next: GraphNode): GraphNode
		= graphNode(type, *next.map { it._id }.toTypedArray())
	private fun graphNode(type: String, vararg next: NodeId, id: NodeId = NodeId()): GraphNode {
		val node = GraphNode(id, USER_ID, NodeDescriptor(Subsystem.INTEGRATION, type), Document(), next.toList())
		service.insertNode(node)
		return node
	}

	@Test
	fun testEmptyFlow() = test {
		val sourceNode = sourceNode()
		val result = service.getFlowForNode_test(sourceNode)
		assertEquals(1, result.size)
		assertEquals(sourceNode._id, result[0]._id)
		assertEquals(0, result[0].next.size)
	}

	@Test
	fun testDirectFlow() = test {
		val target = graphNode("target")
		val source = sourceNode(target)
		val result = service.getFlowForNode_test(source)
		assertEquals(1, result.size)
		val sourceNode = result[0]
		assertEquals(source._id, sourceNode._id)
		assertEquals(1, sourceNode.next.size)
		val targetNode = sourceNode.next[0]
		assertEquals(target._id, targetNode._id)
		assertEquals(0, targetNode.next.size)
	}

	@Test
	fun testDirectFlow_twoTargets() = test {
		val target1 = graphNode("target1")
		val target2 = graphNode("target2")
		val source = sourceNode(target1, target2)
		val result = service.getFlowForNode_test(source)
		assertEquals(1, result.size)
		val sourceNode = result[0].next
		assertEquals(2, sourceNode.size)
		val targetNode1 = sourceNode[0]
		assertEquals(target1._id, targetNode1._id)
		val targetNode2 = sourceNode[1]
		assertEquals(target2._id, targetNode2._id)
		assertEquals(0, targetNode1.next.size)
		assertEquals(0, targetNode2.next.size)
	}

	@Test
	fun testIndirectFlow() = test {
		val target = graphNode("target")
		val mapper = graphNode("mapper", target)
		val source = sourceNode(mapper)
		val result = service.getFlowForNode_test(source)
		assertEquals(1, result.size)
		val sourceResult = result[0]
		assertEquals(source._id, sourceResult._id)
		assertEquals(1, sourceResult.next.size)
		val mapperResult = sourceResult.next[0]
		assertEquals(mapper._id, mapperResult._id)
		assertEquals(1, mapperResult.next.size)
		assertEquals(target._id, mapperResult.next[0]._id)
	}

	@Test
	fun testIndirectFlow_twoTargets() = test {
		val target1 = graphNode("target1")
		val target2 = graphNode("target2")
		val mapper = graphNode("mapper", target1, target2)
		val source = sourceNode(mapper)
		val result = service.getFlowForNode_test(source)
		assertEquals(1, result.size)
		val sourceNode = result[0]
		assertEquals(source._id, sourceNode._id)
		assertEquals(1, sourceNode.next.size)
		val mapperResult = sourceNode.next[0]
		assertEquals(mapper._id, mapperResult._id)
		assertEquals(2, mapperResult.next.size)
		assertEquals(target1._id, mapperResult.next[0]._id)
		assertEquals(target2._id, mapperResult.next[1]._id)
	}

	@Test
	fun testIndirectFlow_recursion() = test {
		val mapper1_id = NodeId()
		val mapper2_id = NodeId()
		val mapper1 = graphNode("mapper1", mapper2_id, id = mapper1_id)
		val mapper2 = graphNode("mapper2", mapper1_id, id = mapper2_id)
		val source = sourceNode(mapper1_id)
		val result = service.getFlowForNode_test(source)
		assertEquals(1, result.size)
		val sourceResult = result[0]
		assertEquals(source._id, sourceResult._id)
		assertEquals(1, sourceResult.next.size)
		val mapperResult = sourceResult.next[0]
		assertEquals(mapper1._id, mapperResult._id)
		assertEquals(1, mapperResult.next.size)
		assertEquals(mapper2._id, mapperResult.next[0]._id)
		assertEquals(0, mapperResult.next[0].next.size)
	}
}
