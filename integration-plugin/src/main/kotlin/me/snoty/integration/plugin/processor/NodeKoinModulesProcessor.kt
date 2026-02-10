package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.plugin.utils.getAnnotation
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

class NodeKoinModulesProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
	override fun process(resolver: Resolver): List<KSAnnotated> {
		// no filtering is done as the `Module` filter would only detect stuff from the last round
		resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.forEach { processClass(it) }

		return emptyList()
	}

	private fun processClass(clazz: KSClassDeclaration) {
		val node = clazz.getAnnotation<RegisterNode>()!!

		val generatedClassName = getKoinModuleClassName(clazz)

		val nodeHandlerModule = TypeSpec.objectBuilder(generatedClassName)
			.addAnnotation(Module::class)
			.addAnnotation(ComponentScan::class)
			.addSerializersModule(node)
			.build()

		val nodeHandlerModuleFileSpec = FileSpec.builder(generatedClassName)
			.addType(nodeHandlerModule)
			.build()

		try {
			nodeHandlerModuleFileSpec.writeTo(
				codeGenerator = codeGenerator,
				aggregating = false,
				originatingKSFiles = listOf(clazz.containingFile!!)
			)
		} catch (_: FileAlreadyExistsException) {}
	}


	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return NodeKoinModulesProcessor(environment.codeGenerator)
		}
	}
}

fun getKoinModuleClassName(clazz: KSClassDeclaration) =
	ClassName(
		clazz.packageName.asString(),
		"${clazz.simpleName.asString().removeSuffix("NodeHandler")}KoinModule"
	)
