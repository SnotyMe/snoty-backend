package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.integration.common.wiring.NodeContextBuilder
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import java.util.*

object NodeHandlerContributorLookup {
	val logger = KotlinLogging.logger {}

	fun executeContributors(nodeRegistry: NodeRegistry, nodeContextBuilder: NodeContextBuilder) {
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
