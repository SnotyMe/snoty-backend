package me.snoty.integration.common.wiring.node.template

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.core.node.NodeType
import me.snoty.core.node.scope
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

object NodeTemplateUtils {
	private val logger = KotlinLogging.logger {}

	@OptIn(ExperimentalPathApi::class)
	private fun provideNodeTemplates(nodeType: NodeType): Sequence<Path>? {
		val nodeDirectory = "/node/${nodeType.value}"
		val resource = javaClass.getResource(nodeDirectory) ?: return let {
			logger.trace { "No node directory found for $nodeType" }
			null
		}

		val root = when {
			resource.path.contains(".jar!") -> {
				logger.trace { "Running in JAR, resolving using FileSystems" }
				val path = Path.of(resource.path.substringBefore("!").substringAfter("file:"))
				FileSystems.newFileSystem(path).getPath(nodeDirectory)
			}
			else -> {
				logger.trace { "Resource is on the classpath, resolving using a regular walk" }
				Path.of(resource.path)
			}
		}

		return root.walk().filter {
			logger.trace { "Checking file ${it.name} for $nodeType" }
			it.isRegularFile() && it.extension == "liquid"
		}
	}

	fun nodeTemplatesModule(nodeType: NodeType) = module {
		provideNodeTemplates(nodeType)
			?.forEach { templateFile ->
				logger.debug { "Registering template ${templateFile.nameWithoutExtension} for $nodeType" }

				val cached = NodeTemplate(
					node = nodeType,
					name = templateFile.nameWithoutExtension,
					template = templateFile.readText(),
				)

				scope(nodeType.scope) {
					factory<NodeTemplate>(named(templateFile.name)) {
						val nodeMetadataFeatureFlags: NodeMetadataFeatureFlags = get()
						if (nodeMetadataFeatureFlags.cacheNodeTemplates) {
							cached
						} else {
							cached.copy(template = templateFile.readText())
						}
					}
				}
			} ?: logger.trace { "Found no templates for $nodeType" }
	}
}
