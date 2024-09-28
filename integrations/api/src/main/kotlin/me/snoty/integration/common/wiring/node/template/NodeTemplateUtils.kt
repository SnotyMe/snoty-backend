package me.snoty.integration.common.wiring.node.template

import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

object NodeTemplateUtils {
	private fun provideNodeTemplates(descriptor: NodeDescriptor) =
		javaClass.getResource("/node/${descriptor.subsystem}/${descriptor.type}")
			?.let { File(it.file) }
			?.walk()
			?.filter { it.isFile && it.extension == "liquid" }

	fun nodeTemplatesModule(descriptor: NodeDescriptor) = module {
		provideNodeTemplates(descriptor)
			?.forEach { templateFile ->
				val cached = NodeTemplate(
					node = descriptor,
					name = templateFile.nameWithoutExtension,
					template = templateFile.readText(),
				)

				factory<NodeTemplate>(named(templateFile.name)) {
					val nodeMetadataFeatureFlags: NodeMetadataFeatureFlags = get()
					if (nodeMetadataFeatureFlags.cacheNodeTemplates) {
						cached
					} else {
						cached.copy(template = templateFile.readText())
					}
				}
			}
	}
}
