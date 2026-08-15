package me.snoty.integration.common.wiring.flow

import me.snoty.backend.hooks.LifecycleHook
import me.snoty.core.flow.Workflow
import me.snoty.core.node.NodeWithSettings

interface FlowManagementService {
	suspend fun deleteFlowCascading(workflow: Workflow)
}

fun interface NodeDeletedHook : LifecycleHook<NodeWithSettings>
