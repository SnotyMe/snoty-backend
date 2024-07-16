package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodeMetadata
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
		val node = clazz.getAnnotationsByType(RegisterNode::class).first()
		val displayName = node.displayName
		val settingsClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::settingsType)
		val inputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::inputType)
		val outputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::outputType)

		val metadata = NodeMetadata(
			displayName = displayName,
			position = node.position,
			settings = generateObjectSchema(settingsClass)!!,
			input = generateObjectSchema(inputClass),
			output = generateObjectSchema(outputClass)
		)

		val fileSpec = FileSpec.scriptBuilder("${clazz.simpleName.asString()}Metadata", clazz.packageName.asString())
			.addCode("internal val metadata = %T(\n", NodeMetadata::class)
			.addDataClassInitializer(metadata)
			.addCode(")\n")
			.build()

		fileSpec
			.writeTo(
				codeGenerator = codeGenerator,
				aggregating = false,
				originatingKSFiles = listOf(clazz.containingFile!!)
			)
	}

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return NodeMetadataProcessor(environment.logger, environment.codeGenerator)
		}
	}
}
