package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toClassNameOrNull
import me.snoty.backend.utils.toTitleCase
import me.snoty.integration.common.model.metadata.*
import kotlin.reflect.KClass

fun generateObjectSchema(resolver: Resolver, clazz: KSClassDeclaration, visited: List<ClassName> = emptyList()): ObjectSchema? {
	when (clazz.toClassName()) {
		NoSchema::class.asClassName() -> return null
		EmptySchema::class.asClassName() -> return emptyList()
	}

	return clazz.getDeclaredProperties().map { prop ->
		val name = prop.simpleName
		val hidden = prop.hasAnnotation<FieldHidden>()
		val censored = prop.hasAnnotation<FieldCensored>()
		val displayName = prop.getAnnotation<FieldName>()?.value ?: name.asString().toTitleCase()
		val description = prop.getAnnotation<FieldDescription>()?.value
		val defaultValue = prop.getAnnotation<FieldDefaultValue>()?.value

		val details = resolver.getDetails(prop, visited)
		NodeField(
			name = name.asString(),
			type = details?.type ?: prop.type.toString(),
			defaultValue = defaultValue,
			displayName = displayName,
			description = description,
			hidden = hidden,
			censored = censored,
			details = details
		)
	}.toList()
}

@OptIn(KspExperimental::class)
fun Resolver.getDetails(
	prop: KSPropertyDeclaration,
	visited: List<ClassName>,
	type: KSType = prop.type.resolve(),
	annotated: KSAnnotated = prop,
): NodeFieldDetails? {
	// work around `KSType#toClassName` throwing an exception when the type has type parameters
	val className = type.toClassNameOrNull() ?: (type.declaration as KSClassDeclaration).toClassName()
	return when {
		Collection::class.isAssignableFrom(type, this) -> {
			val typeRef = type.arguments.single().type!!
			val genericType = typeRef.resolve()
			// get the details of the generic type
			// e.g. `List<@Language("json") String>`
			val fieldDetails = getDetails(prop, visited, genericType, typeRef)
			// wrap into CollectionDetails to potentially add collection metadata later
			NodeFieldDetails.CollectionDetails(fieldDetails)
		}

		Enum::class.isAssignableFrom(type, this) ->
			getEnumDetails(prop)

		String::class.isAssignableFrom(type, this) ->
			NodeFieldDetails.PlaintextDetails(
				lines = annotated.getAnnotation<Multiline>()?.values ?: Multiline.DEFAULT_LINES,
				defaultValue = annotated.getAnnotation<FieldDefaultValue>()?.value ?: "",
				language = annotated.getAnnotation<Language>()?.value
			)

		Map::class.isAssignableFrom(type, this) -> {
			val keyType = type.arguments[0].type!!
			val valueType = type.arguments[1].type!!

			val keyDetails = getDetails(prop, visited, keyType.resolve(), keyType)
			val valueDetails = getDetails(prop, visited, valueType.resolve(), valueType)

			NodeFieldDetails.MapDetails(keyDetails, valueDetails)
		}

		Any::class.isAssignableFrom(type, this) && !className.packageName.startsWith("java") && !className.packageName.startsWith("kotlin") -> {
			if (visited.contains(className)) {
				throw IllegalStateException("Circular reference detected: $visited")
			}

			NodeFieldDetails.ObjectDetails(
				className = className.canonicalName,
				schema = generateObjectSchema(resolver = this, clazz = getKotlinClassByName(className.canonicalName)!!, visited = visited + className)!!
			)
		}

		else -> null
	}
}

@OptIn(KspExperimental::class)
private fun KClass<*>.isAssignableFrom(type: KSType, resolver: Resolver): Boolean {
	return resolver
		.getKotlinClassByName(qualifiedName!!)!!
		.asStarProjectedType()
		.isAssignableFrom(type.makeNotNullable())
}

@OptIn(KspExperimental::class)
private fun Resolver.getEnumDetails(prop: KSPropertyDeclaration): NodeFieldDetails.EnumDetails {
	val test = getKotlinClassByName(prop.type.resolve().toClassName().canonicalName)!!
	val elements = test.declarations
		.filterIsInstance<KSClassDeclaration>()
		.filter { !it.isCompanionObject }
		.map {
			val value = it.simpleName.asString()
			val displayName = it.getAnnotation<DisplayName>()?.value
			NodeFieldDetails.EnumDetails.EnumConstant(value, displayName ?: value)
		}
		.toList()
	return NodeFieldDetails.EnumDetails(elements)
}
