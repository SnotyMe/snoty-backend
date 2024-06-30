package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.NodeDescriptor
import me.snoty.backend.integration.flow.model.Subsystem
import me.snoty.backend.integration.flow.model.graph.Graph
import me.snoty.backend.integration.flow.model.graph.GraphNode
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

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

	private fun graphNode(name: String, vararg next: GraphNode)
		= GraphNode(NodeId(), UUID.randomUUID(), NodeDescriptor(Subsystem.INTEGRATION, name), Document(), next.map { it._id })
	private fun graphNode(name: String, id: NodeId, vararg next: NodeId)
		= GraphNode(id, UUID.randomUUID(), NodeDescriptor(Subsystem.INTEGRATION, name), Document(), next.toList())

	@Test
	fun testDirectFlow() = test {
		val target = graphNode("target")
		val result = createFlowFromGraph(Graph(listOf(target._id), listOf(target)))
		assertEquals(1, result.size)
		assertEquals(target._id, result[0].id)
		assertEquals(0, result[0].next.size)
	}

	@Test
	fun testDirectFlow_twoTargets() = test {
		val target1 = graphNode("target1")
		val target2 = graphNode("target2")
		val result = createFlowFromGraph(Graph(listOf(target1._id, target2._id), listOf(target1, target2)))
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
		val result = createFlowFromGraph(Graph(listOf(mapper._id), listOf(mapper, target)))
		assertEquals(1, result.size)
		assertEquals(mapper._id, result[0].id)
		assertEquals(1, result[0].next.size)
		assertEquals(target._id, result[0].next[0].id)
	}

	@Test
	fun testIndirectFlow_twoTargets() = test {
		val target1 = graphNode("target1")
		val target2 = graphNode("target2")
		val mapper = graphNode("mapper", target1, target2)
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
		val mapper1 = graphNode("mapper1", mapper1_id, mapper2_id)
		val mapper2 = graphNode("mapper2", mapper2_id, mapper1_id)
		val result = createFlowFromGraph(Graph(listOf(mapper1._id), listOf(mapper1, mapper2)))
		assertEquals(1, result.size)
		val mapperResult = result[0]
		assertEquals(mapper1._id, mapperResult.id)
		assertEquals(1, mapperResult.next.size)
		assertEquals(mapper2._id, mapperResult.next[0].id)
		assertEquals(0, mapperResult.next[0].next.size)
	}
}
