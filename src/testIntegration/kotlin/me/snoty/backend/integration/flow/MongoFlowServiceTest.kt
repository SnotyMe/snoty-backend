package me.snoty.backend.integration.flow

import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.NodeDescriptor
import me.snoty.backend.integration.flow.model.Subsystem
import me.snoty.backend.integration.flow.model.graph.GraphNode
import me.snoty.backend.test.MongoTest
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class MongoFlowServiceTest : AbstractFlowFetchTest<MongoFlowServiceTest.FlowTestContextImpl>(::FlowTestContextImpl) {
	data class FlowTestContextImpl(
		override var flow: List<FlowNode>? = null
	) : FlowTestContext

	private val mongoDB = MongoTest.getMongoDatabase {}
	private val service = object : MongoFlowService(mongoDB) {
		context(FlowTestContext)
		fun getFlowForNode_test(node: GraphNode) = runBlocking {
			val flow = getFlowForNode(FlowNode(node._id, node.descriptor, node.config, emptyList())).toCollection(mutableListOf())
			this@FlowTestContext.flow = flow
			flow
		}

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
		val node = GraphNode(id, UUID.randomUUID(), NodeDescriptor(Subsystem.INTEGRATION, type), Document(), next.toList())
		val result = service.insertNode(node)
		println(result)
		return node
	}

	@Test
	fun testEmptyFlow() = test {
		val result = service.getFlowForNode_test(sourceNode())
		assertEquals(0, result.size)
	}

	@Test
	fun testDirectFlow() = test {
		val target = graphNode("target")
		val result = service.getFlowForNode_test(sourceNode(target))
		assertEquals(1, result.size)
		assertEquals(target._id, result[0].id)
		assertEquals(0, result[0].next.size)
	}

	@Test
	fun testDirectFlow_twoTargets() = test {
		val target1 = graphNode("target1")
		val target2 = graphNode("target2")
		val result = service.getFlowForNode_test(sourceNode(target1, target2))
		assertEquals(2, result.size)
		assertEquals(target1._id, result[0].id)
		assertEquals(target2._id, result[1].id)
		assertEquals(0, result[0].next.size)
		assertEquals(0, result[1].next.size)
	}

	@Test
	fun testIndirectFlow() = test {
		val target = graphNode("target")
		val mapper = graphNode("mapper", target)
		val result = service.getFlowForNode_test(sourceNode(mapper))
		assertEquals(1, result.size)
		val mapperResult = result[0]
		assertEquals(mapper._id, mapperResult.id)
		assertEquals(1, mapperResult.next.size)
		assertEquals(target._id, mapperResult.next[0].id)
	}

	@Test
	fun testIndirectFlow_twoTargets() = test {
		val target1 = graphNode("target1")
		val target2 = graphNode("target2")
		val mapper = graphNode("mapper", target1, target2)
		val result = service.getFlowForNode_test(sourceNode(mapper))
		assertEquals(1, result.size)
		val mapperResult = result[0]
		assertEquals(mapper._id, mapperResult.id)
		assertEquals(2, mapperResult.next.size)
		assertEquals(target1._id, mapperResult.next[0].id)
		assertEquals(target2._id, mapperResult.next[1].id)
	}

	@Test
	fun testIndirectFlow_recursion() = test {
		val mapper1_id = NodeId()
		val mapper2_id = NodeId()
		val mapper1 = graphNode("mapper1", mapper2_id, id = mapper1_id)
		val mapper2 = graphNode("mapper2", mapper1_id, id = mapper2_id)
		val result = service.getFlowForNode_test(sourceNode(mapper1_id))
		assertEquals(1, result.size)
		val mapperResult = result[0]
		assertEquals(mapper1._id, mapperResult.id)
		assertEquals(1, mapperResult.next.size)
		assertEquals(mapper2._id, mapperResult.next[0].id)
		assertEquals(0, mapperResult.next[0].next.size)
	}
}
