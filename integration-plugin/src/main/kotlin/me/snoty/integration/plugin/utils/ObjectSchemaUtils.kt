package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import me.snoty.integration.common.model.metadata.*
import kotlin.reflect.KClass

fun generateObjectSchema(resolver: Resolver, clazz: KSClassDeclaration): ObjectSchema? {
	when (clazz.toClassName()) {
		NoSchema::class.asClassName() -> return null
		EmptySchema::class.asClassName() -> return emptyList()
	}

	return clazz.getDeclaredProperties().map { prop ->
		val name = prop.simpleName
		val hidden = prop.hasAnnotation<FieldHidden>()
		val censored = prop.hasAnnotation<FieldCensored>()
		val displayName = prop.getAnnotation<FieldName>()?.value ?: name.asString()
		val description = prop.getAnnotation<FieldDescription>()?.value

		val details = resolver.getDetails(prop)
		NodeField(
			name = name.asString(),
			type = details?.valueType ?: prop.type.toString(),
			displayName = displayName,
			description = description,
			hidden = hidden,
			censored = censored,
			details = details
		)
	}.toList()
}

fun Resolver.getDetails(prop: KSPropertyDeclaration): NodeFieldDetails? {
	val type = prop.type.resolve()
	return when {
		Enum::class.isAssignableFrom(type) ->
			getEnumDetails(prop)
		String::class.isAssignableFrom(type) ->
			NodeFieldDetails.PlaintextDetails(
				lines = prop.getAnnotation<Multiline>()?.values ?: Multiline.DEFAULT_LINES,
			)
		else -> null
	}
}

context(Resolver)
@OptIn(KspExperimental::class)
private fun KClass<*>.isAssignableFrom(type: KSType): Boolean {
	return getKotlinClassByName(qualifiedName!!)!!.asStarProjectedType().isAssignableFrom(type.makeNotNullable())
}

context(Resolver)
@OptIn(KspExperimental::class)
private fun getEnumDetails(prop: KSPropertyDeclaration): NodeFieldDetails.EnumDetails {
	val test = getKotlinClassByName(prop.type.resolve().toClassName().canonicalName)!!
	val elements = test.declarations
		.filterIsInstance<KSClassDeclaration>()
		.filter { !it.isCompanionObject }
		.map {
			val value = it.simpleName.asString()
			val displayName = it.getAnnotation<FieldName>()?.value
			NodeFieldDetails.EnumDetails.EnumConstant(value, displayName ?: value)
		}
		.toList()
	return NodeFieldDetails.EnumDetails(elements)
}
