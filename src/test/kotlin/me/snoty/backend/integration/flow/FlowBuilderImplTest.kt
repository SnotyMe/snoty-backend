package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.test.TYPE_MAP
import me.snoty.integration.common.wiring.graph.Graph
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class FlowBuilderImplTest {
	private val uuid: UUID = UUID.randomUUID()
	private val flowBuilder = FlowBuilderImpl {
		EmptyNodeSettings()
	}

	private fun graphNode(descriptor: NodeDescriptor, next: List<NodeId>? = null, id: NodeId = NodeId()) = GraphNode(
		id,
		uuid,
		descriptor,
		Document(),
		next
	)

	@Test
	fun `test createFlowFromGraph basic`() {
		val graph = Graph(
			involvedNodes = emptyList(),
			rootNext = emptyList()
		)
		val result = flowBuilder.createFlowFromGraph(graph)
		assertEquals(0, result.size)

		val graphNode2 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val graph2 = Graph(
			involvedNodes = listOf(
				graphNode2
			),
			rootNext = listOf(
				graphNode2._id
			)
		)
		val result2 = flowBuilder.createFlowFromGraph(graph2)
		assertEquals(1, result2.size)
		assertEquals(graphNode2._id, result2[0]._id)
	}

	@Test
	fun `test createFlowFromGraph two next`() {
		val graphNode1 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val graphNode2 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val graph = Graph(
			involvedNodes = listOf(
				graphNode1,
				graphNode2
			),
			rootNext = listOf(
				graphNode1._id,
				graphNode2._id
			)
		)
		val result = flowBuilder.createFlowFromGraph(graph)
		assertEquals(2, result.size)
		assertEquals(graphNode1._id, result[0]._id)
		assertEquals(graphNode2._id, result[1]._id)
	}

	@Test
	fun `test createFlowFromGraph circular`() {
		val graphNode1 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))
		val graphNode2 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), listOf(graphNode1._id))
		val graph = Graph(
			involvedNodes = listOf(
				graphNode1,
				graphNode2
			),
			rootNext = listOf(
				graphNode2._id
			)
		)
		val result = flowBuilder.createFlowFromGraph(graph)
		assertEquals(1, result.size)
		val node1Result = result[0]
		assertEquals(graphNode2._id, node1Result._id)
		assertEquals(1, node1Result.next.size)
		val node2Result = node1Result.next[0]
		assertEquals(graphNode1._id, node2Result._id)
		// recursion detection
		assertEquals(0, node2Result.next.size)
	}

	@Test
	fun `test createFlowFromGraph circleBack`() {
		val rootNext1Id = NodeId()
		val rootNext2Id = NodeId()
		val next2ProceedId = NodeId()

		val rootNext1 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), id=rootNext1Id)
		val rootNext2 = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), listOf(next2ProceedId), id=rootNext2Id)
		// circles back to the rootNext2
		val next2Proceed = graphNode(NodeDescriptor(Subsystem.INTEGRATION, "publisher"), listOf(rootNext2Id), id=next2ProceedId)

		val graph = Graph(
			involvedNodes = listOf(
				rootNext1,
				rootNext2,
				next2Proceed
			),
			rootNext = listOf(
				rootNext1Id,
				rootNext2Id
			)
		)

		val result = flowBuilder.createFlowFromGraph(graph)
		assertEquals(2, result.size)
		assertEquals(rootNext1Id, result[0]._id)
		assertEquals(rootNext2Id, result[1]._id)
		val rootNext2Result = result[1]
		assertEquals(1, rootNext2Result.next.size)
		assertEquals(next2ProceedId, rootNext2Result.next[0]._id)
		val next2ProceedResult = rootNext2Result.next[0]
		assertEquals(0, next2ProceedResult.next.size)
	}

	@Test
	fun `test createFlowFromGraph complex`() {
		val discordNode = graphNode(NodeDescriptor(Subsystem.INTEGRATION, "publisher"))
		val mapNode = graphNode(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP), listOf(discordNode._id))
		val graph = Graph(
			rootNext = listOf(
				discordNode._id,
				mapNode._id
			),
			involvedNodes = listOf(
				mapNode,
				discordNode
			)
		)
		val result = flowBuilder.createFlowFromGraph(graph)
		assertEquals(2, result.size)
		assertEquals(discordNode._id, result[0]._id)
		assertEquals(mapNode._id, result[1]._id)
		val mapResult = result[1]
		assertEquals(1, mapResult.next.size)
		assertEquals(discordNode._id, mapResult.next[0]._id)
	}
}
