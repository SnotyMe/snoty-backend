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

	fun executeContributors() {
		val loader = ServiceLoader.load(NodeHandlerContributor::class.java)

		val result = loader.map {
			registerContributor(it)
				.onFailure { ex ->
					val exception = prettify(ex)
					val isFatal = reportStartupFailure(it, exception)

					if (isFatal && featureFlags.crashOnStartupFailure) {
						throw exception
					}
				}
		}

		val successCount = result.count { it.isSuccess }
		val allCount = result.size
		logger.info { "Successfully enabled $successCount / $allCount node handlers!" }
	}

	private fun registerContributor(contributor: NodeHandlerContributor) = runCatching {
		logger.debug { "Adding from ${contributor.javaClass.simpleName}..." }

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
		logger.info { "Successfully enabled ${contributor.metadata.descriptor.id}!" }
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
				is ConfigFailure.MissingConfigValue -> logger.warn {
					"Didn't enable $nodeName because the required config of type ${fail.type.simpleClassName} is missing"
				}

				is ConfigFailure.DataClassFieldErrors if fail.type.jvmErasure.hasAnnotation<ConfigWrapper>() -> logger.warn {
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
