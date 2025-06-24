package me.snoty.backend.wiring.node

import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.getScopeName

object NodesScope {
	val scopeId = getScopeName().value
	val scopeQualifier = getScopeName()
}

@Single(createdAtStart = true)
class InitNodesScope : KoinComponent {
	init {
		getKoin().createScope(NodesScope.scopeId, NodesScope.scopeQualifier)
	}
}
