package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.plugin.utils.quoted

class NodeHandlerContributorProcessor(val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {

	private val allProcessingResults = mutableListOf<ProcessResult>()
	private var kspFileWritten = false

	@OptIn(KspExperimental::class)
	override fun process(resolver: Resolver): List<KSAnnotated> {
		val allElements = resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.toList()

		val available = allElements
			.filter {
				resolver.getDeclarationsFromPackage(it.packageName.asString())
					.filterIsInstance<KSPropertyDeclaration>()
					.any { declaration ->
						declaration.type.toTypeName() == NodeMetadata::class.asTypeName()
					}
			}

		available.forEach {
			val processResult = processClass(it)
			allProcessingResults.add(processResult)
		}

		val unprocessedClasses = (allElements - available.toSet()).toList()
		// if there are no more unprocessed classes, return them and don't write the SPI file yet
		if (unprocessedClasses.isNotEmpty() || kspFileWritten) {
			return unprocessedClasses
		}

		// write SPI file
		codeGenerator.createNewFileByPath(
			dependencies = Dependencies(
				aggregating = false,
				*(allProcessingResults.map { it.containingFile }.toTypedArray())
			),
			path = "META-INF/services/${NodeHandlerContributor::class.qualifiedName}",
			extensionName = ""
		).writer().use {
			allProcessingResults.forEach { contributor ->
				it.appendLine(contributor.contributorClassName.canonicalName)
			}
		}
		kspFileWritten = true

		return unprocessedClasses
	}

	data class ProcessResult(val contributorClassName: ClassName, val containingFile: KSFile)

	@OptIn(KspExperimental::class)
	private fun processClass(clazz: KSClassDeclaration): ProcessResult {
		val node = clazz.getAnnotationsByType(RegisterNode::class).first()

		val contributorClassName = ClassName(clazz.packageName.asString(), "${clazz.simpleName.asString()}Contributor")
		val classBuilder = TypeSpec.classBuilder(contributorClassName)
		val fileSpec = FileSpec.builder(contributorClassName)

		classBuilder
			.addSuperinterface(NodeHandlerContributor::class)
			.addFunction(createNodeHandlerContributorFun(clazz, node))

		// write contributor file
		fileSpec
			.addType(classBuilder.build())
			.build()
			.writeTo(
				codeGenerator = codeGenerator,
				aggregating = false,
				originatingKSFiles = listOf(clazz.containingFile!!)
			)

		return ProcessResult(contributorClassName = contributorClassName, containingFile = clazz.containingFile!!)
	}

	private fun createNodeHandlerContributorFun(handler: KSClassDeclaration, node: RegisterNode): FunSpec {
		val abstractFun = NodeHandlerContributor::class.toTypeSpec(lenient = true)
			.funSpecs
			.first()
		val funSpec = abstractFun
			.toBuilder()
			.apply {
				modifiers -= KModifier.ABSTRACT
				modifiers += KModifier.OVERRIDE
			}
			.addStatement("val nodeContext = nodeContextBuilder(metadata.descriptor)")
			.addStatement("val handler = %T(nodeContext)", handler.toClassName())
			.addStatement("registry.registerHandler(metadata, handler)")

		return funSpec.build()
	}

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return NodeHandlerContributorProcessor(environment.logger, environment.codeGenerator)
		}
	}
}
