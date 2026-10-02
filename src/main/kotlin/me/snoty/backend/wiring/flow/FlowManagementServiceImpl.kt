package me.snoty.backend.wiring.flow

import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.backend.wiring.node.NodeService
import me.snoty.core.flow.Workflow
import org.koin.core.annotation.Single

@Single
class FlowManagementServiceImpl(
    private val flowExecutionService: FlowExecutionService,
    private val nodeService: NodeService,
    private val flowService: FlowService,
) : FlowManagementService {
    override suspend fun deleteFlowCascading(workflow: Workflow) {
        flowExecutionService.deleteAll(workflow)
        flowService.getWithNodes(workflow.userId, workflow.id)?.nodes?.forEach {
            nodeService.delete(it)
        }
        flowService.delete(workflow)
    }
}
