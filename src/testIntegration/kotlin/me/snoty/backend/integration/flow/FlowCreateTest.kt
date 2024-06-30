package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.graph.Graph
import me.snoty.backend.integration.flow.model.graph.GraphNode
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlowCreateTest : AbstractFlowFetchTest<FlowCreateTest.FlowTestContextImpl>(::FlowTestContextImpl) {

	data class FlowTestContextImpl(
		override var flow: List<FlowNode>? = null
	) : FlowTestContext {
		fun createFlowFromGraph(graph: Graph) = FlowBuilderImpl.createFlowFromGraph(graph).toList()
	}

	@Test
	fun testEmptyFlow() = test {
		val result = createFlowFromGraph(Graph(emptyList(), emptyList()))
		assertEquals(0, result.size)
	}

	@Test
	fun testDirectFlow() = test {
		val target = GraphNode(NodeId(), "target", Document(), emptyList())
		val result = createFlowFromGraph(Graph(listOf(target._id), listOf(target)))
		assertEquals(1, result.size)
		assertEquals(target._id, result[0].id)
		assertEquals(0, result[0].next.size)
	}

	@Test
	fun testDirectFlow_twoTargets() = test {
		val target1 = GraphNode(NodeId(), "target1", Document(), emptyList())
		val target2 = GraphNode(NodeId(), "target2", Document(), emptyList())
		val result = createFlowFromGraph(Graph(listOf(target1._id, target2._id), listOf(target1, target2)))
		assertEquals(2, result.size)
		assertEquals(target1._id, result[0].id)
		assertEquals(target2._id, result[1].id)
		assertEquals(0, result[0].next.size)
		assertEquals(0, result[1].next.size)
	}

	@Test
	fun testIndirectFlow() = test {
		val target = GraphNode(NodeId(), "target", Document(), emptyList())
		val mapper = GraphNode(NodeId(), "mapper", Document(), listOf(target._id))
		val result = createFlowFromGraph(Graph(listOf(mapper._id), listOf(mapper, target)))
		assertEquals(1, result.size)
		assertEquals(mapper._id, result[0].id)
		assertEquals(1, result[0].next.size)
		assertEquals(target._id, result[0].next[0].id)
	}

	@Test
	fun testIndirectFlow_twoTargets() = test {
		val target1 = GraphNode(NodeId(), "target1", Document(), emptyList())
		val target2 = GraphNode(NodeId(), "target2", Document(), emptyList())
		val mapper = GraphNode(NodeId(), "mapper", Document(), listOf(target1._id, target2._id))
		val result = createFlowFromGraph(Graph(listOf(mapper._id), listOf(mapper, target1, target2)))
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
		val mapper1 = GraphNode(mapper1_id, "mapper1", Document(), listOf(mapper2_id))
		val mapper2 = GraphNode(mapper2_id, "mapper2", Document(), listOf(mapper1_id))
		val result = createFlowFromGraph(Graph(listOf(mapper1._id), listOf(mapper1, mapper2)))
		assertEquals(1, result.size)
		val mapperResult = result[0]
		assertEquals(mapper1._id, mapperResult.id)
		assertEquals(1, mapperResult.next.size)
		assertEquals(mapper2._id, mapperResult.next[0].id)
		assertEquals(0, mapperResult.next[0].next.size)
	}
}
