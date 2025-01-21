package me.snoty.backend.wiring.node

import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.getScopeName

object NodesScope

@Single(createdAtStart = true)
class InitNodesScope : KoinComponent {
	init {
		getKoin().createScope(NodesScope.getScopeName().value, NodesScope.getScopeName())
	}
}
