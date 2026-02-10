package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
	private val allProcessingResults = mutableListOf<SpiContributor>()

	private fun contributorName(clazz: KSClassDeclaration)
		= ClassName(clazz.packageName.asString(), "${clazz.simpleName.asString()}Contributor")

	@OptIn(KspExperimental::class)
	override fun process(resolver: Resolver): List<KSAnnotated> {
		val newlyProcessed = resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.filter {
				resolver.getClassDeclarationByName(contributorName(it).canonicalName) == null
			}
			.onEach { handler ->
				handler.getAnnotation<Single>() ?:
					logger.error("NodeHandlerContributorProcessor: ${handler.qualifiedName!!.asString()} is missing @Single annotation", handler)
			}
			.onEach { handler ->
				logger.info("NodeHandlerContributorProcessor: processing ${handler.qualifiedName!!.asString()}...")

				val processResult = processClass(resolver, handler)
				allProcessingResults.add(processResult)

				handler.qualifiedName!!.asString()
			}
			.toList()

		logger.info("NodeHandlerContributorProcessor: newly processed: $newlyProcessed")
		if (newlyProcessed.isEmpty()) return emptyList()

		codeGenerator.writeSpiFile(serviceQualifiedName = NodeHandlerContributor::class.qualifiedName!!, services = allProcessingResults)

		return emptyList()
	}

	private fun processClass(resolver: Resolver, clazz: KSClassDeclaration): SpiContributor {
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
					.single { it.name == NodeHandlerContributor::metadata.name }
					.override()
					.initializer("%S", generateMetadata(resolver, clazz, registerNode))
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

		return SpiContributor(contributorClassName = contributorClassName, containingFile = clazz.containingFile!!)
	}

	private fun PropertySpec.Builder.buildKoinInitializer(clazz: KSClassDeclaration) = apply {
		val koinModuleClassName = getKoinModuleClassName(clazz)
		initializer(
			CodeBlock.builder()
				.add("listOf(\n")
				.add(
					"%T.%M(),\n",
					koinModuleClassName,
					MemberName(koinModuleClassName.packageName, "module"),
				)
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
