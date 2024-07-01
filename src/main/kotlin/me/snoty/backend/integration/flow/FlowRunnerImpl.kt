package me.snoty.backend.integration.flow

import kotlinx.coroutines.flow.*
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.flow.FlowOutput
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.node.NodeRegistry

class FlowRunnerImpl(private val nodeRegistry: NodeRegistry) : FlowRunner {
	override fun execute(node: RelationalFlowNode, input: EdgeVertex): Flow<FlowOutput> {
		val processor = nodeRegistry.lookupHandler(node.descriptor)
			?: return flowOf(FlowOutput("No handler for descriptor ${node.descriptor}", node._id))

		return flow {
				emit(processor.process(node, input))
			}.flatMapConcat { output ->
				node.next.asFlow()
					.flatMapConcat {
						execute(it, output)
					}
			}
	}
}
