package me.snoty.backend.wiring.flow

import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowManagementService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.NodeDeletedHook
import me.snoty.integration.common.wiring.flow.Workflow
import org.koin.core.annotation.Single

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
