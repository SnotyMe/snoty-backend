package me.snoty.backend.integration

import dev.openfeature.sdk.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.scope
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.*

@Single
class NodeHandlerContributorLookupFeatureFlags(override val client: Client) : FeatureFlagsContainer {
	val crashOnStartupFailure by FeatureFlagBoolean(
		"nodeHandlerContributorLookup.crashOnStartupFailure",
		false
	)
}

@Single
class NodeHandlerContributorLookup(private val koin: Koin, private val featureFlags: NodeHandlerContributorLookupFeatureFlags) {
	val logger = KotlinLogging.logger {}

	val nodeRegistry: NodeRegistry by koin.inject()

	fun executeContributors() {
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)

		loader.forEach {
			val result = registerContributor(it)
			if (result.isFailure) {
				logger.error(result.exceptionOrNull()) { "Failed to add from ${it.javaClass.simpleName}" }

				if (featureFlags.crashOnStartupFailure) {
					throw result.exceptionOrNull()!!
				}
			}
		}

		val count = loader.count()
		logger.info { "Executed $count NodeHandlerContributors" }
	}

	private fun registerContributor(contributor: NodeHandlerContributor) = runCatching {
		logger.info { "Adding from ${contributor.javaClass.simpleName}..." }

		val scopeName = contributor.metadata.descriptor.scope
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
}
