package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import me.snoty.integration.common.model.metadata.*

fun generateObjectSchema(clazz: KSClassDeclaration): ObjectSchema? {
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
		NodeField(
			name = name.asString(),
			type = prop.type.toString(),
			displayName = displayName,
			description = description,
			hidden = hidden,
			censored = censored
		)
	}.toList()
}
