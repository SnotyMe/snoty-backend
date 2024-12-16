package me.snoty.integration.common.wiring.node.template

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.scope
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

object NodeTemplateUtils {
	private val logger = KotlinLogging.logger {}

	@OptIn(ExperimentalPathApi::class)
	private fun provideNodeTemplates(descriptor: NodeDescriptor): Sequence<Path>? {
		val nodeDirectory = "/node/${descriptor.namespace}/${descriptor.name}"
		val resource = javaClass.getResource(nodeDirectory) ?: return let {
			logger.trace { "No node directory found for ${descriptor.id}" }
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
			logger.trace { "Checking file ${it.name} for ${descriptor.id}" }
			it.isRegularFile() && it.extension == "liquid"
		}
	}

	fun nodeTemplatesModule(descriptor: NodeDescriptor) = module {
		provideNodeTemplates(descriptor)
			?.forEach { templateFile ->
				logger.debug { "Registering template ${templateFile.nameWithoutExtension} for ${descriptor.id}" }

				val cached = NodeTemplate(
					node = descriptor,
					name = templateFile.nameWithoutExtension,
					template = templateFile.readText(),
				)

				scope(descriptor.scope) {
					factory<NodeTemplate>(named(templateFile.name)) {
						val nodeMetadataFeatureFlags: NodeMetadataFeatureFlags = get()
						if (nodeMetadataFeatureFlags.cacheNodeTemplates) {
							cached
						} else {
							cached.copy(template = templateFile.readText())
						}
					}
				}
			} ?: logger.trace { "Found no templates for ${descriptor.id}" }
	}
}
