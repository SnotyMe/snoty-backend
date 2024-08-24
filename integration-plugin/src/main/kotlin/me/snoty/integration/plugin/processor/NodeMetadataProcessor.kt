package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.plugin.utils.addDataClassInitializer
import me.snoty.integration.plugin.utils.generateObjectSchema
import me.snoty.integration.plugin.utils.resolveClassFromAnnotation

class NodeMetadataProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
	// avoid re-run on incremental compilation
	private var hasRun = false

	override fun process(resolver: Resolver): List<KSAnnotated> {
		if (hasRun) {
			return emptyList()
		}
		resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.forEach { processClass(resolver, it) }
		hasRun = true

		return emptyList()
	}

	@OptIn(KspExperimental::class)
	private fun processClass(resolver: Resolver, clazz: KSClassDeclaration) {
		val node = clazz.getAnnotationsByType(RegisterNode::class).single()
		val displayName = node.displayName
		val settingsClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::settingsType)
		val inputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::inputType)
		val outputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::outputType)

		val metadata = NodeMetadata(
			descriptor = NodeDescriptor(subsystem = node.subsystem, type = node.type),
			displayName = displayName,
			position = node.position,
			settings = generateObjectSchema(resolver, settingsClass)!!,
			input = generateObjectSchema(resolver, inputClass),
			output = generateObjectSchema(resolver, outputClass)
		)

		val fileSpec = FileSpec.scriptBuilder("${clazz.simpleName.asString()}Metadata", clazz.packageName.asString())
			.addCode("internal val $NODE_METADATA = %T(\n", NodeMetadata::class)
			.addDataClassInitializer(metadata)
			.addCode(")\n")
			.addSerializersModule(node)
			.build()

		fileSpec
			.writeTo(
				codeGenerator = codeGenerator,
				aggregating = false,
				originatingKSFiles = listOf(clazz.containingFile!!)
			)
	}

	companion object {
		const val NODE_METADATA = "nodeMetadata"
	}

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return NodeMetadataProcessor(environment.logger, environment.codeGenerator)
		}
	}
}
