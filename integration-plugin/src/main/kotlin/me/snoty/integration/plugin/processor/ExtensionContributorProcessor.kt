package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import me.snoty.extension.ExtensionContributor
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.plugin.utils.*
import me.snoty.integration.plugin.utils.koin.KoinEntities
import me.snoty.integration.plugin.utils.koin.KoinScopeReferences
import me.snoty.integration.plugin.utils.koin.writeKoinScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

class ExtensionContributorProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    companion object {
        const val METADATA_PACKAGE = "me.snoty.extension"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val extensionName = resolver.getExtensionName()
        val contributorName = ClassName(METADATA_PACKAGE,"${extensionName}ExtensionContributor")

        if (resolver.getClassDeclarationByName(contributorName.canonicalName) != null) {
            // already written, don't do it again
            return emptyList()
        }

        val koinModuleName = ClassName(METADATA_PACKAGE, "${extensionName}ExtensionKoinModule")

        val koinScope = codeGenerator.writeKoinScope(
            METADATA_PACKAGE,
            "${extensionName}Extension",
            "extension:$extensionName",
        )
        writeKoinModule(resolver, extensionName, koinScope, koinModuleName)
        val koinEntities = KoinEntities(
            scope = koinScope,
            moduleClassName = koinModuleName,
        )
        writeExtensionContributor(contributorName, koinEntities)

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

    private fun writeKoinModule(resolver: Resolver, extensionName: String, koinScope: KoinScopeReferences, koinModuleName: ClassName) {
        val nodes = resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        val extensionPackages = nodes
            // me.simulatan.snoty.myintegration.mynode.MyNodeHandler -> me.simulatan.snoty.myintegration
            // every handler has its own dedicated package, so we need to walk upwards by one to
            // get a "generic"-ish package that hopefully contains shared code, such as API clients
            // opinionated logic that may not hold in all cases, but it'll have to do for now
            .map { it.packageName.asString().substringBeforeLast(".") }
            .toList()
            .distinct()

        val commonExtensionPackages = groupCommonPackages(extensionPackages)
        val koinModule = TypeSpec.objectBuilder(koinModuleName)
            .addAnnotation(Module::class)
            .addAnnotation(AnnotationSpec.get(ComponentScan(*commonExtensionPackages.toTypedArray())))
            .addSerializersModule(nodes.mapNotNull { it.getAnnotation<RegisterNode>() }.toList(), extensionName, koinScope)
            .build()

        val koinModuleFileSpec = FileSpec.builder(koinModuleName)
            .addType(koinModule)
            .build()

        koinModuleFileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = false,
        )
    }

    private fun writeExtensionContributor(contributorName: ClassName, koinEntities: KoinEntities) {
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
                        koinEntities.moduleClassName,
                        MemberName(METADATA_PACKAGE, "module"),
                    )
                    .build()
            )
            .addProperty(
                contributorSpec
                    .propertySpecs
                    .single { it.name == ExtensionContributor::koinScope.name }
                    .override()
                    .initializer("%L", koinEntities.scope.scopeProperty.name)
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
