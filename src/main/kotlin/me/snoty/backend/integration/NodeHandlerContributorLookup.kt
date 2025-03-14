package me.snoty.backend.integration

import com.sksamuel.hoplite.ConfigFailure
import dev.openfeature.sdk.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.ConfigException
import me.snoty.backend.config.ConfigWrapper
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import me.snoty.backend.utils.simpleClassName
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.scope
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.core.error.InstanceCreationException
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.util.*
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

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

	/**
	 * Stage 1: loading all Koin DI modules
	 */
	private fun loadKoinModules(): List<Pair<NodeHandlerContributor, Scope>> {
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)
		return loader.map { contributor ->
			val scopeName = contributor.metadata.descriptor.scope
			val scope = koin.getOrCreateScope(scopeName.value, scopeName)
			
			logger.trace { "Loading modules for $scopeName..." }
			koin.loadModules(listOf(module {
				includes(contributor.koinModules)
				scope(scope.scopeQualifier) {
					scoped { contributor.metadata }
					scoped { contributor.metadata.descriptor }
				}
			}))
			
			contributor to scope
		}
	}

	/**
	 * Stage 2: creating the NodeHandler instances and registering them
	 * This will initialize non-eager services needed. Since all modules are registered, all Koin declarations should be available.
	 */
	private fun registerNodeHandler(contributor: NodeHandlerContributor, scope: Scope) = runCatching {
		logger.trace { "Adding from ${contributor.javaClass.simpleName}..." }

		val handler: NodeHandler = scope.get(
			clazz = contributor.nodeHandlerClass,
			parameters = { parametersOf(contributor.metadata, contributor.metadata.descriptor) }
		)
		nodeRegistry.registerHandler(contributor.metadata, handler)
		logger.info { "Successfully enabled ${contributor.metadata.descriptor.id}!" }
	}

	fun loadAndRegisterNodeHandlers() {
		logger.debug { "Loading all extension modules..." }
		val nodeHandlerContributors = loadKoinModules()
		
		logger.debug { "Loaded ${nodeHandlerContributors.size} extension modules." }

		logger.debug { "Registering node handlers..." }
		val result = nodeHandlerContributors.map { (contributor, scope) ->
			registerNodeHandler(contributor, scope)
				.onFailure { ex ->
					val exception = prettify(ex)
					val isFatal = reportStartupFailure(contributor, exception)

					if (isFatal && featureFlags.crashOnStartupFailure) {
						throw exception
					}
				}
		}

		val successCount = result.count { it.isSuccess }
		val allCount = result.size
		logger.info { "Successfully enabled $successCount / $allCount node handlers!" }
	}

	private fun prettify(e: Throwable): Throwable = when (val cause = e.cause) {
		null -> e
		// unwrap recursively to the root cause
		else if e is InstanceCreationException -> prettify(cause)
		else -> e
	}

	private fun reportStartupFailure(contributor: NodeHandlerContributor, exception: Throwable?): Boolean {
		val nodeName = contributor.metadata.descriptor.id
		if (exception is ConfigException) {
			var fatal: Boolean? = false
			when (val fail = exception.fail) {
				// the user hasn't configured it -> probably unwanted
				is ConfigFailure.MissingConfigValue -> logger.info {
					"Didn't enable $nodeName because the required config of type ${fail.type.simpleClassName} is missing"
				}

				// the user hasn't configured a wrapper -> probably unwanted
				// wrappers only contain the actual wanted field and are simply used to allow sealed classes to work
				is ConfigFailure.DataClassFieldErrors if fail.type.jvmErasure.hasAnnotation<ConfigWrapper>() -> logger.info {
					val wrapperName = fail.type.simpleClassName
					val fields = fail.errors.list.joinToString(", ") {
						(it as? ConfigFailure.MissingConfigValue)?.type?.simpleClassName ?: wrapperName
					}
					"Didn't enable $nodeName because the required config(s) of type(s) $fields is missing"
				}

				// couldn't handle the issue, fallthrough
				else -> fatal = null
			}

			if (fatal != null) return fatal
		}

		logger.error(exception) { "Failed to enable $nodeName" }
		return true
	}
}
