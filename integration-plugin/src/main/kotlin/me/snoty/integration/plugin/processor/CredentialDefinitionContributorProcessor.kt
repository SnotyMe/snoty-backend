package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import me.snoty.backend.utils.toTitleCase
import me.snoty.backend.wiring.credential.CredentialDefinitionContributor
import me.snoty.backend.wiring.credential.RegisterCredential
import me.snoty.backend.wiring.node.metadataJson
import me.snoty.integration.plugin.utils.*

class CredentialDefinitionContributorProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
	private val loggerPrefix = this::class.simpleName

	private val allProcessingResults = mutableListOf<SpiContributor>()

	private fun contributorName(clazz: KSClassDeclaration)
		= ClassName(clazz.packageName.asString(), "${clazz.simpleName.asString()}Contributor")

	override fun process(resolver: Resolver): List<KSAnnotated> {
		val newlyProcessed = resolver.getSymbolsWithAnnotation(RegisterCredential::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.filter {
				resolver.getClassDeclarationByName(contributorName(it).canonicalName) == null
			}
			.onEach { credential ->
				logger.info("$loggerPrefix: processing ${credential.qualifiedName!!.asString()}...")

				val processResult = processClass(resolver, credential)
				allProcessingResults.add(processResult)
			}
			.toList()

		logger.info("${this::class.simpleName}: newly processed: $newlyProcessed")
		if (newlyProcessed.isEmpty()) return emptyList()

		codeGenerator.writeSpiFile(serviceQualifiedName = CredentialDefinitionContributor::class.qualifiedName!!, services = allProcessingResults)

		return emptyList()
	}

	private fun processClass(resolver: Resolver, clazz: KSClassDeclaration): SpiContributor {
		val contributorClassName = contributorName(clazz)

		val classBuilder = TypeSpec.classBuilder(contributorClassName)
		val fileSpec = FileSpec.builder(contributorClassName)

		val contributorSpec = CredentialDefinitionContributor::class.toTypeSpec(lenient = true)

		val registerCredential = clazz.getAnnotation<RegisterCredential>()!!
		val objectSchema = generateObjectSchema(resolver, clazz)

		classBuilder
			.addSuperinterface(CredentialDefinitionContributor::class)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == CredentialDefinitionContributor::type.name }
					.override()
					.initializer("%S", registerCredential.type)
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == CredentialDefinitionContributor::displayName.name }
					.override()
					.initializer("%S", registerCredential.displayName.ifEmpty { registerCredential.type.toTitleCase() })
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == CredentialDefinitionContributor::clazz.name }
					.override()
					.initializer("%T::class.java", clazz.toClassName())
					.build()
			)
			.addProperty(
				contributorSpec
					.propertySpecs
					.single { it.name == CredentialDefinitionContributor::schema.name }
					.override()
					.initializer("%S", metadataJson.encodeToString(objectSchema))
					.build()
			)

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

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment) =
			CredentialDefinitionContributorProcessor(environment.logger, environment.codeGenerator)
	}
}
