package me.snoty.integration.common.wiring.flow

import me.snoty.backend.hooks.LifecycleHook
import me.snoty.integration.common.wiring.Node

interface FlowManagementService {
	suspend fun deleteFlowCascading(workflow: Workflow)
}

fun interface NodeDeletedHook : LifecycleHook<Node>
