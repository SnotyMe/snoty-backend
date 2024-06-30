package me.snoty.backend.integration.flow

import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.node.NodeRegistry

fun interface FlowRunner {
	fun execute(rootNext: List<FlowNode>, input: EdgeVertex): List<FlowOutput>
}

class FlowRunnerImpl(private val nodeRegistry: NodeRegistry) : FlowRunner {
	override fun execute(rootNext: List<FlowNode>, input: EdgeVertex): List<FlowOutput> {
		return rootNext.flatMap {
			executeNode(it, input)
		}
	}

	private fun executeNode(node: FlowNode, input: EdgeVertex): List<FlowOutput> {
		val processor = nodeRegistry.lookupHandler(node.descriptor)
			?: return listOf(FlowOutput("No handler for descriptor ${node.descriptor}", node.id))

		val output = processor.process(node, input)

		return node.next.flatMap {
			executeNode(it, output)
		}
	}
}
