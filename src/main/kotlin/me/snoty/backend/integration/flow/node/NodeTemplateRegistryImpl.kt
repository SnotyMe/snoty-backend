package me.snoty.backend.integration.flow.node

import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.template.NodeTemplate
import me.snoty.integration.common.wiring.node.template.NodeTemplateRegistry
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class NodeTemplateRegistryImpl(private val koin: Koin) : NodeTemplateRegistry {
	override fun getAllTemplates(): Map<NodeDescriptor, List<NodeTemplate>> {
		// get it from koin to allow refreshing in dev mode
		return koin.getAll<NodeTemplate>()
			.groupBy { it.node }
	}
}
