package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import me.snoty.extension.ExtensionContributor
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.plugin.utils.SpiContributor
import me.snoty.integration.plugin.utils.groupCommonPackages
import me.snoty.integration.plugin.utils.override
import me.snoty.integration.plugin.utils.writeSpiFile
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

class ExtensionContributorProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    companion object {
        const val METADATA_PACKAGE = "me.snoty.extension"
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val extensionName = resolver.getModuleName().getShortName()
            .replaceFirstChar(Char::uppercase)
            .replace("-(\\w)".toRegex()) { matchResult ->
                matchResult.groupValues[1].uppercase()
            }
        val contributorName = ClassName(METADATA_PACKAGE,"${extensionName}ExtensionContributor")

        if (resolver.getClassDeclarationByName(contributorName.canonicalName) != null) {
            // already written, don't do it again
            return emptyList()
        }

        val koinModuleName = ClassName(METADATA_PACKAGE, "${extensionName}KoinModule")

        writeKoinModule(resolver, koinModuleName)
        writeExtensionContributor(contributorName = contributorName, koinModuleName = koinModuleName)

        codeGenerator.writeSpiFile(
            serviceQualifiedName = ExtensionContributor::class.qualifiedName!!,
            services = listOf(SpiContributor(
                contributorClassName = contributorName,
                containingFile = null,
            )),
            aggregating = false,
        )

        return emptyList()
    }

    private fun writeKoinModule(resolver: Resolver, koinModuleName: ClassName) {
        val extensionPackages = resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            // me.simulatan.snoty.myintegration.mynode.MyNodeHandler -> me.simulatan.snoty.myintegration
            // every handler has its own dedicated package, so we need to walk upwards by one to
            // get a "generic"-ish package that hopefully contains shared code, such as API clients
            // opinionated logic that may not hold in all cases, but it'll have to do for now
            .map { it.packageName.asString().substringBeforeLast(".") }
            .distinct()
            .toList()

        val commonExtensionPackages = groupCommonPackages(extensionPackages)
        val koinModule = TypeSpec.objectBuilder(koinModuleName)
            .addAnnotation(Module::class)
            .addAnnotation(AnnotationSpec.get(ComponentScan(*commonExtensionPackages.toTypedArray())))
            .build()

        val koinModuleFileSpec = FileSpec.builder(koinModuleName)
            .addType(koinModule)
            .build()

        koinModuleFileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = false,
        )
    }

    private fun writeExtensionContributor(contributorName: ClassName, koinModuleName: ClassName) {
        val contributorSpec = ExtensionContributor::class.toTypeSpec(lenient = true)

        val contributor = TypeSpec.classBuilder(contributorName)
            .addSuperinterface(ExtensionContributor::class)
            .addProperty(
                contributorSpec
                    .propertySpecs
                    .single { it.name == ExtensionContributor::koinModule.name }
                    .override()
                    .initializer(
                        "%T.%M()",
                        koinModuleName,
                        MemberName(METADATA_PACKAGE, "module"),
                    )
                    .build()
            )
            .build()

        val contributorFileSpec = FileSpec.builder(contributorName)
            .addType(contributor)
            .build()

        contributorFileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = true,
        )
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return ExtensionContributorProcessor(environment.logger, environment.codeGenerator)
        }
    }
}
