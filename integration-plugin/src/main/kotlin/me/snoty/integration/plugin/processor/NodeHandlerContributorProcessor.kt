package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.template.NodeTemplateUtils
import me.snoty.integration.plugin.utils.*
import org.koin.core.annotation.Single

class NodeHandlerContributorProcessor(val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {

	private val allProcessingResults = mutableListOf<ProcessResult>()
	private var kspFileWritten = false

	private fun contributorName(clazz: KSClassDeclaration)
		= ClassName(clazz.packageName.asString(), "${clazz.simpleName.asString()}Contributor")

	@OptIn(KspExperimental::class)
	override fun process(resolver: Resolver): List<KSAnnotated> {
		val allElements = resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.toList()

		allElements.mapNotNull { handler ->
			handler.getAnnotation<Single>() ?:
				logger.error("NodeHandlerContributorProcessor: ${handler.qualifiedName!!.asString()} is missing @Single annotation", handler)
		}

		val ready = allElements
			.filter {
				resolver.getDeclarationsFromPackage(it.packageName.asString())
					.filterIsInstance<KSPropertyDeclaration>()
					.any { declaration ->
						declaration.simpleName.getShortName() == NodeMetadataProcessor.NODE_METADATA
					}
			}

		ready
			.forEach {
				val processResult = processClass(resolver, it)
				allProcessingResults.add(processResult)
			}

		val unprocessedClasses = (allElements - ready.toSet()).toList()
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

	private fun processClass(resolver: Resolver, clazz: KSClassDeclaration): ProcessResult {
		val contributorClassName = contributorName(clazz)

		val classBuilder = TypeSpec.classBuilder(contributorClassName)
		val fileSpec = FileSpec.builder(contributorClassName)

		val contributorSpec = NodeHandlerContributor::class.toTypeSpec(lenient = true)

		val registerNode = clazz.getAnnotation<RegisterNode>()!!
		val nodeMetadata = registerNode.descriptor(clazz)
		classBuilder
			.addSuperinterface(NodeHandlerContributor::class)
			.addProperty(
				PropertySpec.builder("descriptor", NodeDescriptor::class)
					.initializer(
						"%T(%S, %S)",
						NodeDescriptor::class,
						nodeMetadata.namespace,
						nodeMetadata.name,
					)
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == NodeHandlerContributor::nodeHandlerClass.name }
					.override()
					.initializer("%T::class", clazz.toClassName())
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == NodeHandlerContributor::metadataV2.name }
					.override()
					.initializer(NodeMetadataProcessor.NODE_METADATA)
					.removeModifiers(KModifier.OPEN) // w: 'open' has no effect on a final class
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == NodeHandlerContributor::settingsClass.name }
					.override()
					.initializer("%T::class", resolver.resolveClassFromAnnotation(clazz, RegisterNode::settingsType).toClassName())
					.removeModifiers(KModifier.OPEN) // w: 'open' has no effect on a final class
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == NodeHandlerContributor::koinModules.name }
					.override()
					.buildKoinInitializer(clazz)
					.build()
			)

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

	private fun PropertySpec.Builder.buildKoinInitializer(clazz: KSClassDeclaration) = apply {
		fun CodeBlock.Builder.addModule(moduleClassName: ClassName): CodeBlock.Builder {
			return apply {
				add(
					"%T.%M,\n",
					moduleClassName,
					MemberName("org.koin.ksp.generated", "module"),
				)
			}
		}

		initializer(
			CodeBlock.builder()
				.add("listOf(\n")
				.add("%M,\n", MemberName("org.koin.ksp.generated", "defaultModule"))
				.addModule(getGeneratedModule(clazz))
				.add(
					"%M(%N),\n",
					NodeTemplateUtils::nodeTemplatesModule.getMemberName<NodeTemplateUtils>(),
					"descriptor",
				)
				.add(")")
				.build()
		)
	}

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return NodeHandlerContributorProcessor(environment.logger, environment.codeGenerator)
		}
	}
}
