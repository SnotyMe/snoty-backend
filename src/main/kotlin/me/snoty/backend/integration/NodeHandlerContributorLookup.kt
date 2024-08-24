package me.snoty.backend.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.Koin
import org.koin.core.annotation.Single
import java.util.*

class NodeHandlerContributorLookup(private val koin: Koin) {
	val logger = KotlinLogging.logger {}

	val nodeRegistry: NodeRegistry by koin.inject()

	fun executeContributors() {
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)

		loader.forEach {
			logger.info { "Adding from ${it.javaClass.simpleName}..." }
			nodeRegistry.registerHandler(it.metadata, koin.get(clazz = it.nodeHandlerClass))
			logger.info { "Added from ${it.javaClass.simpleName}!" }
		}

		val count = loader.count()
		logger.info { "Executed $count NodeHandlerContributors" }
	}
}

val nodeHandlerContributors = ServiceLoader.load(NodeHandlerContributor::class.java).toList()

typealias NodeHandlerContributorList = List<NodeHandlerContributor>
@Single
fun provideNodeHandlerContributors(): NodeHandlerContributorList {
	return nodeHandlerContributors
}
