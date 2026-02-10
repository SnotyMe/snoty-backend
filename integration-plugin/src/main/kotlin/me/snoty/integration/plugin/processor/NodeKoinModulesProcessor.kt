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
import me.snoty.integration.plugin.utils.getAnnotation
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Scope

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

		val nodeHandlerScope = TypeSpec.classBuilder(getKoinClassName(clazz, "Scope"))
			.build()

		val nodeHandlerModule = TypeSpec.objectBuilder(getKoinClassName(clazz, "Module"))
			.addAnnotation(
				AnnotationSpec.builder(Scope::class)
					.addMember("value = %L::class", nodeHandlerScope.name!!)
					.build()
			)
			.addAnnotation(Module::class)
			.addAnnotation(ComponentScan::class)
			.addSerializersModule(node, nodeHandlerScope)
			.build()

		val nodeHandlerModuleFileSpec = FileSpec.builder(getKoinClassName(clazz, ""))
			.addType(nodeHandlerScope)
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

fun getKoinClassName(clazz: KSClassDeclaration, entity: String) =
	ClassName(
		clazz.packageName.asString(),
		"${clazz.simpleName.asString().removeSuffix("Handler")}Koin${entity}"
	)
