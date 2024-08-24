package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getKotlinClassByName
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
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.plugin.utils.getAnnotation
import me.snoty.integration.plugin.utils.override
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

/*
		allElements.mapNotNull { handler ->
			handler.getAnnotation<Single>() ?:
				logger.error("NodeHandlerContributorProcessor: ${handler.qualifiedName!!.asString()} is missing @Single annotation")
		}
*/

		val ready = allElements
			.filter {
				resolver.getDeclarationsFromPackage(it.packageName.asString())
					.filterIsInstance<KSPropertyDeclaration>()
					.any { declaration ->
						declaration.type.toTypeName() == NodeMetadata::class.asTypeName()
					}
			}

		ready
			.forEach {
				val processResult = try {
					processClass(it)
				} catch (e: FileAlreadyExistsException) {
					throw e
					return@forEach
				}
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

	private fun processClass(clazz: KSClassDeclaration): ProcessResult {
		val contributorClassName = contributorName(clazz)

		val classBuilder = TypeSpec.classBuilder(contributorClassName)
		val fileSpec = FileSpec.builder(contributorClassName)

		val contributorSpec = NodeHandlerContributor::class.toTypeSpec(lenient = true)

		classBuilder
			.addSuperinterface(NodeHandlerContributor::class)
			// .addFunction(createNodeHandlerContributorFun(clazz))
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
					.single { it.name == NodeHandlerContributor::metadata.name }
					.override()
					.initializer(NodeMetadataProcessor.NODE_METADATA)
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == NodeHandlerContributor::koin.name }
					.override()
					.buildKoinInitializer()
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

	private fun PropertySpec.Builder.buildKoinInitializer() = apply {
		initializer(
			"""
				%M {
					%M()
				}.koin
			""".trimIndent(),
			MemberName("org.koin.dsl", "koinApplication"),
			MemberName("org.koin.ksp.generated", "defaultModule")
		)
	}

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return NodeHandlerContributorProcessor(environment.logger, environment.codeGenerator)
		}
	}
}
