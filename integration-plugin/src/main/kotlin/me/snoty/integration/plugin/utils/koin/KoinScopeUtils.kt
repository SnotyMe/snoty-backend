package me.snoty.integration.plugin.utils.koin

import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo

data class KoinScopeReferences(
	val scopeValueProperty: PropertySpec,
	val scopeProperty: PropertySpec,
)

fun CodeGenerator.writeKoinScope(packageName: String, entityName: String, scopeValue: String): KoinScopeReferences {
	val scopeValueProperty = PropertySpec.builder(
		"${entityName}KoinScopeValue",
		STRING,
		KModifier.CONST,
	)
		.initializer("%S", scopeValue)
		.build()
	val stringQualifier = ClassName("org.koin.core.qualifier", "StringQualifier")
	val scopeProperty = PropertySpec.builder("${entityName}KoinScope", stringQualifier)
		.initializer("%T(%L)", stringQualifier, scopeValueProperty.name)
		.build()

	val scopeFile = FileSpec.builder(ClassName(packageName, "${entityName}Koin"))
		.addProperty(scopeValueProperty)
		.addProperty(scopeProperty)
		.build()

	scopeFile.writeTo(
		codeGenerator = this,
		aggregating = false,
	)

	return KoinScopeReferences(
		scopeValueProperty = scopeValueProperty,
		scopeProperty = scopeProperty,
	)
}
