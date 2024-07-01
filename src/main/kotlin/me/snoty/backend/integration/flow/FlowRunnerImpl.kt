package me.snoty.backend.integration.flow

import kotlinx.coroutines.flow.*
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.flow.FlowOutput
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.slf4j.Logger

class FlowRunnerImpl(private val nodeRegistry: NodeRegistry) : FlowRunner {
	override fun execute(logger: Logger, node: RelationalFlowNode, input: EdgeVertex): Flow<FlowOutput>
		= executeImpl(logger, node, input)

	private fun executeImpl(logger: Logger, node: RelationalFlowNode, input: EdgeVertex, depth: Int = 0): Flow<FlowOutput> {
		val processor = nodeRegistry.lookupHandler(node.descriptor)
			?: return flowOf(FlowOutput("No handler for descriptor ${node.descriptor}", node._id))

		return flow {
			logger.debug("Processing node {} with input {} at depth {}", node.descriptor, input, depth)
			emit(processor.process(node, input))
			logger.debug("Processed node {}", node.descriptor)
		}.flatMapConcat { output ->
			node.next.asFlow()
				.flatMapConcat {
					logger.debug("Processing next {} with output {}", it.descriptor, output)
					executeImpl(logger, it, output, depth + 1)
				}
		}
	}
}
