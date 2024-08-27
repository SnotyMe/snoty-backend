package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.plugin.GENERATED_PACKAGE
import me.snoty.integration.plugin.utils.getAnnotation
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

class NodeKoinModulesProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
	override fun process(resolver: Resolver): List<KSAnnotated> {
		resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
			.toList()
			.ifEmpty {
				resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
					.filterIsInstance<KSClassDeclaration>()
					.forEach { processClass(it) }
			}


		return emptyList()
	}

	private fun processClass(clazz: KSClassDeclaration) {
		val node = clazz.getAnnotation<RegisterNode>()!!

		val generatedModule = TypeSpec.objectBuilder(getGeneratedModule(clazz))
			.addAnnotation(Module::class)
			// .addAnnotation(AnnotationSpec.get(Scope(name = clazz.simpleName.asString())))
			.addSerializersModule(node)
			.build()

		val defaultModule = TypeSpec.objectBuilder(getDefaultModule(clazz))
			.addAnnotation(Module::class)
			.addAnnotation(AnnotationSpec.get(ComponentScan()))
			.build()

		val fileSpec = FileSpec.builder(GENERATED_PACKAGE, "${clazz.simpleName.asString()}KoinModules")
			.addType(generatedModule)
			.addType(defaultModule)
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
			return NodeKoinModulesProcessor(environment.logger, environment.codeGenerator)
		}
	}
}

fun getDefaultModule(clazz: KSClassDeclaration) = ClassName(GENERATED_PACKAGE, "${clazz.simpleName.asString()}DefaultModule")
fun getGeneratedModule(clazz: KSClassDeclaration) = ClassName(GENERATED_PACKAGE, "${clazz.simpleName.asString()}GeneratedModule")
