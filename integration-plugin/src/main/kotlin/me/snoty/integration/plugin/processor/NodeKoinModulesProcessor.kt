package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.plugin.utils.getAnnotation
import me.snoty.integration.plugin.utils.getExtensionName
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

class NodeKoinModulesProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
	override fun process(resolver: Resolver): List<KSAnnotated> {
		// no filtering is done as the `Module` filter would only detect stuff from the last round
		resolver.getSymbolsWithAnnotation(RegisterNode::class.qualifiedName!!)
			.filterIsInstance<KSClassDeclaration>()
			.forEach { processClass(resolver, it) }

		return emptyList()
	}

	private fun processClass(resolver: Resolver, clazz: KSClassDeclaration) {
		val node = clazz.getAnnotation<RegisterNode>()!!

		val extensionName = resolver.getExtensionName()
		val nodeHandlerScopeValue = PropertySpec.builder(
			getKoinClassName(clazz, "ScopeValue").simpleName,
			STRING,
			KModifier.CONST,
		)
			.initializer("%S", "${extensionName}:${node.name}")
			.build()
		val stringQualifier = ClassName("org.koin.core.qualifier", "StringQualifier")
		val nodeHandlerScope = PropertySpec.builder(getKoinClassName(clazz, "Scope").simpleName, stringQualifier)
			.initializer("%T(%L)", stringQualifier, nodeHandlerScopeValue.name)
			.build()

		val nodeHandlerModule = TypeSpec.objectBuilder(getKoinClassName(clazz, "Module"))
			.addAnnotation(Module::class)
			.addAnnotation(ComponentScan::class)
			.addSerializersModule(node, nodeHandlerScopeValue)
			.build()

		val nodeHandlerModuleFileSpec = FileSpec.builder(getKoinClassName(clazz, ""))
			.addProperty(nodeHandlerScopeValue)
			.addProperty(nodeHandlerScope)
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
