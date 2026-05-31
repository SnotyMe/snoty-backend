package me.snoty.integration.plugin.processor.node

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.plugin.utils.koin.KoinEntities
import me.snoty.integration.plugin.utils.koin.writeKoinScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

fun CodeGenerator.writeNodeKoinEntities(clazz: KSClassDeclaration, extensionName: String, node: RegisterNode): KoinEntities {
	val koinScope = writeKoinScope(
		clazz.packageName.asString(),
		entityName = clazz.simpleName.asString(),
		scopeValue = "extension:${extensionName}:node:${node.name}",
	)

	val moduleName = ClassName(clazz.packageName.asString(), "${clazz.simpleName.asString()}KoinModule")
	val nodeHandlerModule = TypeSpec.objectBuilder(moduleName)
		.addAnnotation(Module::class)
		.addAnnotation(ComponentScan::class)
		.build()

	val nodeHandlerModuleFileSpec = FileSpec.builder(moduleName)
		.addType(nodeHandlerModule)
		.build()

	try {
		nodeHandlerModuleFileSpec.writeTo(
			codeGenerator = this,
			aggregating = false,
			originatingKSFiles = listOf(clazz.containingFile!!)
		)
	} catch (_: FileAlreadyExistsException) {}

	return KoinEntities(
		scope = koinScope,
		moduleClassName = moduleName,
	)
}
