package me.snoty.backend.integration

import com.sksamuel.hoplite.ConfigFailure
import dev.openfeature.sdk.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.ConfigException
import me.snoty.backend.config.ConfigWrapper
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import me.snoty.backend.utils.simpleClassName
import me.snoty.backend.wiring.node.NodesScope
import me.snoty.backend.wiring.node.metadataJson
import me.snoty.integration.common.model.metadata.NodeMetadata
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

	data class NodeHandlerContributorData(
		val contributor: NodeHandlerContributor,
		val metadata: NodeMetadata,
		val scope: Scope
	)

	/**
	 * Stage 1: loading all Koin DI modules
	 */
	private fun loadKoinModules(): List<NodeHandlerContributorData> {
		val nodesScope = koin.getScope(NodesScope.scopeId)
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)
		return loader.map { contributor ->
			@Suppress("DEPRECATION") // still used for backwards compatibility
			val metadata =
				contributor.metadataV2
					?.let { metadataJson.decodeFromString<NodeMetadata>(it) }
					?.copy(settingsClass = contributor.settingsClass!!)
				?: contributor.metadata
				?: throw IllegalStateException("NodeHandlerContributor ${contributor.javaClass.simpleName} has no metadata")

			val scopeName = metadata.descriptor.scope
			val scope = koin.getOrCreateScope(scopeName.value, scopeName)
			nodesScope.linkTo(scope) // link to discover node scoped services without explicit @ScopeId on usage

			logger.trace { "Loading modules for $scopeName..." }
			koin.loadModules(listOf(module {
				includes(contributor.koinModules)
				scope(scope.scopeQualifier) {
					scoped { metadata }
					scoped { metadata.descriptor }
				}
			}))
			
			NodeHandlerContributorData(contributor, metadata, scope)
		}
	}

	/**
	 * Stage 2: creating the NodeHandler instances and registering them
	 * This will initialize non-eager services needed. Since all modules are registered, all Koin declarations should be available.
	 */
	private fun registerNodeHandler(contributor: NodeHandlerContributor, metadata: NodeMetadata, scope: Scope) = runCatching {
		logger.trace { "Adding from ${contributor.javaClass.simpleName}..." }

		val handler: NodeHandler = scope.get(
			clazz = contributor.nodeHandlerClass,
			parameters = { parametersOf(metadata, metadata.descriptor) }
		)
		nodeRegistry.registerHandler(metadata, handler)
		logger.info { "Successfully enabled ${metadata.descriptor.id}!" }
	}

	fun loadAndRegisterNodeHandlers() {
		logger.debug { "Loading all extension modules..." }
		val nodeHandlerContributors = loadKoinModules()
		
		logger.debug { "Loaded ${nodeHandlerContributors.size} extension modules." }

		logger.debug { "Registering node handlers..." }
		val result = nodeHandlerContributors.map { (contributor, metadata, scope) ->
			registerNodeHandler(contributor, metadata, scope)
				.onFailure { ex ->
					val exception = prettify(ex)
					val isFatal = reportStartupFailure(metadata, exception)

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

	private fun reportStartupFailure(metadata: NodeMetadata, exception: Throwable?): Boolean {
		val nodeName = metadata.descriptor.id
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
