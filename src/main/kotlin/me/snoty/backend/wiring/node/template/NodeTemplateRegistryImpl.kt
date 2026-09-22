package me.snoty.backend.wiring.node.template

import me.snoty.backend.injection.getFromAllScopes
import me.snoty.core.node.NodeType
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class NodeTemplateRegistryImpl(private val koin: Koin) : NodeTemplateRegistry {
	override fun getAllTemplates(): Map<NodeType, List<NodeTemplate>> {
		// get it from koin to allow refreshing in dev mode
		return koin.getFromAllScopes<NodeTemplate>()
			.groupBy { it.node }
	}
}
