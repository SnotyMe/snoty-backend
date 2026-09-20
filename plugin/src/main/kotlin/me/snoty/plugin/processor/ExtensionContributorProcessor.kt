package me.snoty.plugin.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.extension.ExtensionContributor
import me.snoty.plugin.utils.*
import me.snoty.plugin.utils.koin.KoinEntities
import me.snoty.plugin.utils.koin.writeKoinScope
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
        val koinEntities = KoinEntities(
            scope = koinScope,
            moduleClassName = koinModuleName,
        )
        writeKoinModule(resolver, extensionName, koinEntities)
        writeExtensionContributor(contributorName, koinEntities)

        codeGenerator.writeSpiFile(
            serviceQualifiedName = ExtensionContributor::class.qualifiedName!!,
            services = listOf(
                SpiContributor(
                    contributorClassName = contributorName,
                    containingFile = null,
                )
            ),
            aggregating = false,
        )

        return emptyList()
    }

    private fun writeKoinModule(resolver: Resolver, extensionName: String, koinEntities: KoinEntities) {
        val nodes = resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        // terrible workaround for https://github.com/InsertKoinIO/koin-compiler-plugin/issues/107
        val usedPackages = resolver.getAllFiles()
            .filter {
				it.declarations
					.filterIsInstance<KSClassDeclaration>()
                    .any()
			}
            .map { it.packageName.asString() }
            .toSet()
            .toTypedArray()

        val registerNodeAnnotations = nodes.mapNotNull { it.getAnnotation<RegisterNode>() }.toList()
        val koinModule = TypeSpec.objectBuilder(koinEntities.moduleClassName)
            .addAnnotation(Module::class)
            .addAnnotation(AnnotationSpec.get(ComponentScan(*usedPackages)))
            .addSerializersModule(registerNodeAnnotations, extensionName, koinEntities.scope)
            .build()

        val koinModuleFileSpec = FileSpec.builder(koinEntities.moduleClassName)
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
                contributorSpec.overrideProperty(ExtensionContributor::koinModule)
                    .initializer(
                        "%T.%M()",
                        koinEntities.moduleClassName,
                        MemberName(METADATA_PACKAGE, "module"),
                    )
                    .build()
            )
            .addProperty(
                contributorSpec.overrideProperty(ExtensionContributor::koinScope)
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
