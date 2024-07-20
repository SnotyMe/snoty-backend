package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import me.snoty.integration.common.model.metadata.*

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

		val details = getDetails(resolver, prop)
		NodeField(
			name = name.asString(),
			type = details?.second ?: prop.type.toString(),
			displayName = displayName,
			description = description,
			hidden = hidden,
			censored = censored,
			details = details?.first
		)
	}.toList()
}

@OptIn(KspExperimental::class)
fun getDetails(resolver: Resolver, prop: KSPropertyDeclaration): Pair<NodeFieldDetails, String>? {
	val type = prop.type.resolve()
	return when {
		resolver.getKotlinClassByName(Enum::class.qualifiedName!!)!!.asStarProjectedType().isAssignableFrom(type) -> {
			val test = resolver.getKotlinClassByName(prop.type.resolve().toClassName().canonicalName)!!
			val elements = test.declarations
				.filterIsInstance<KSClassDeclaration>()
				.filter { !it.isCompanionObject }
				.map {
					val value = it.simpleName.asString()
					val displayName = it.getAnnotation<FieldName>()?.value
					NodeFieldDetails.EnumDetails.EnumConstant(value, displayName ?: value)
				}
				.toList()
			Pair(NodeFieldDetails.EnumDetails(elements), "Enum")
		}
		else -> null
	}
}
