package me.snoty.backend.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.*

class NodeHandlerContributorLookup(private val koin: Koin) {
	val logger = KotlinLogging.logger {}

	val nodeRegistry: NodeRegistry by koin.inject()

	fun executeContributors() {
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)

		loader.forEach { contributor ->
			logger.info { "Adding from ${contributor.javaClass.simpleName}..." }

			val scopeName = named(contributor.metadata.descriptor.toString())
			koin.loadModules(listOf(module {
				includes(contributor.koinModules)
				scope(scopeName) {
					scoped { contributor.metadata }
					scoped { contributor.metadata.descriptor }
				}
			}))

			val scope = koin.getOrCreateScope(scopeName.value, scopeName)
			val handler: NodeHandler = scope.get(
				clazz = contributor.nodeHandlerClass,
				parameters = { parametersOf(contributor.metadata, contributor.metadata.descriptor) }
			)
			nodeRegistry.registerHandler(contributor.metadata, handler)
			logger.info { "Added from ${contributor.javaClass.simpleName}!" }
		}

		val count = loader.count()
		logger.info { "Executed $count NodeHandlerContributors" }
	}
}
