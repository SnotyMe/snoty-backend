package me.snoty.backend.wiring.node

import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.wiring.flow.NodeCreatedHook
import me.snoty.backend.wiring.flow.NodeDeletedHook
import me.snoty.backend.wiring.flow.NodeUpdatedHook
import me.snoty.core.flow.Workflow
import me.snoty.core.node.Node
import me.snoty.core.node.NodeType
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.user.UserId
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class NodeServiceInterceptor(
    @Named("adapter") private val delegate: NodeService,
    private val hookRegistry: HookRegistry,
) : NodeService by delegate {
    override suspend fun <S : NodeSettings> create(
        userId: UserId,
        flow: Workflow,
        type: NodeType,
        name: String,
        position: NodePosition,
        settings: S
    ) =
        delegate.create(userId, flow, type, name, position, settings).also {
            hookRegistry.executeHooks(NodeCreatedHook::class, it)
        }

    override suspend fun patch(node: Node, patchRequest: NodePatch): ServiceResult =
        delegate.patch(node, patchRequest).also {
            if (it !is NodeServiceResults.NodeUpdated) return@also

            hookRegistry.executeHooks(NodeUpdatedHook::class, it.node)
        }

    override suspend fun updateSettings(node: Node, settings: NodeSettings): ServiceResult =
        delegate.updateSettings(node, settings).also {
            if (it !is NodeServiceResults.NodeUpdated) return@also

            hookRegistry.executeHooks(NodeUpdatedHook::class, it.node)
        }

    override suspend fun delete(node: NodeWithSettings) =
        delegate.delete(node).also {
            if (it !is NodeServiceResults.NodeDeleted) return@also

            hookRegistry.executeHooks(NodeDeletedHook::class, node)
        }
}
