package me.snoty.backend.wiring.flow

import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.backend.wiring.node.NodeService
import me.snoty.core.flow.Workflow
import org.koin.core.annotation.Single

@Single
class FlowManagementServiceImpl(
    private val flowScheduler: FlowScheduler,
    private val flowExecutionService: FlowExecutionService,
    private val nodeService: NodeService,
    private val flowService: FlowService,
    private val hookRegistry: HookRegistry,
) : FlowManagementService {
    override suspend fun deleteFlowCascading(workflow: Workflow) {
        flowScheduler.deleteAll(workflow)
        flowExecutionService.deleteAll(workflow)
        flowService.getWithNodes(workflow.userId, workflow.id)?.nodes?.forEach {
            hookRegistry.executeHooks(NodeDeletedHook::class, it)
            nodeService.delete(it)
        }
        flowService.delete(workflow)
    }
}
