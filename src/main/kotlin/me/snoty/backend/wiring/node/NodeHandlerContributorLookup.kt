package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.integration.common.NodeContextBuilder
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import java.util.*

object NodeHandlerContributorLookup {
	val logger = KotlinLogging.logger {}

	fun executeContributors(nodeRegistry: NodeRegistryImpl, nodeContextBuilder: NodeContextBuilder) {
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)

		loader.forEach {
			logger.info { "Adding from ${it.javaClass.simpleName}..." }
			it.contributeHandlers(nodeRegistry, nodeContextBuilder)
			logger.info { "Added from ${it.javaClass.simpleName}!" }
		}

		val count = loader.count()
		logger.info { "Executed $count NodeHandlerContributors" }
	}
}
