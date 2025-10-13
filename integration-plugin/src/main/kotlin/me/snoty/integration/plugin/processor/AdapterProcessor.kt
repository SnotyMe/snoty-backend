package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.squareup.kotlinpoet.ksp.toClassName
import me.snoty.backend.adapter.Adapter
import me.snoty.integration.plugin.utils.SpiContributor
import me.snoty.integration.plugin.utils.writeSpiFile

class AdapterProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor, KSTopDownVisitor<KSFile, Unit>() {
	override fun process(resolver: Resolver): List<KSAnnotated> {
		resolver.getNewFiles()
			.forEach { it.accept(this, it) }

		return emptyList()
	}

	override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: KSFile) {
		// logger.warn(classDeclaration.getAllSuperTypes().toList().toString())
		val adapter = classDeclaration.getAllSuperTypes()
			.singleOrNull {
				it.declaration
					.closestClassDeclaration()
					?.superTypes
					?.any { superType -> superType.resolve().declaration.qualifiedName?.asString() == Adapter::class.qualifiedName } == true
			} ?: return

		logger.warn("Found adapter: ${classDeclaration.simpleName.asString()} is impl for ${adapter.toClassName().simpleName}", data)

		codeGenerator.writeSpiFile(
			adapter.toClassName().canonicalName,
			listOf(SpiContributor(classDeclaration.toClassName(), data))
		)
	}

	override fun defaultHandler(node: KSNode, data: KSFile) = Unit

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return AdapterProcessor(environment.logger, environment.codeGenerator)
		}
	}
}
