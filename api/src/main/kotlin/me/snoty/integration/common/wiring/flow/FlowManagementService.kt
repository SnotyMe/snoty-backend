package me.snoty.integration.common.wiring.flow

import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.LifecycleHook
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.Node
import org.koin.core.annotation.Single

interface FlowManagementService {
	suspend fun deleteFlowCascading(workflow: Workflow)
}

fun interface NodeDeletedHook : LifecycleHook<Node>

@Single
class MongoFlowManagementService(
	private val flowScheduler: FlowScheduler,
	private val flowExecutionService: FlowExecutionService,
	private val nodeService: NodeService,
	private val flowService: FlowService,
	private val hookRegistry: HookRegistry,
) : FlowManagementService {
	override suspend fun deleteFlowCascading(workflow: Workflow) {
		flowScheduler.deleteAll(workflow)
		flowExecutionService.deleteAll(workflow._id)
		flowService.getWithNodes(workflow._id)?.nodes?.forEach {
			hookRegistry.executeHooks(NodeDeletedHook::class, it)
			nodeService.delete(it._id)
		}
		flowService.delete(workflow._id)
	}
}
