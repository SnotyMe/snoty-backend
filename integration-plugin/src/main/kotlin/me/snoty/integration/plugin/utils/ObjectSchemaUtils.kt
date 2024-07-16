package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import me.snoty.integration.common.model.NodeField
import me.snoty.integration.common.model.ObjectSchema
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldHidden

fun generateObjectSchema(clazz: KSClassDeclaration): ObjectSchema? {
	if (clazz.toClassName() == Unit::class.asClassName()) return null

	return clazz.getDeclaredProperties().map { prop ->
		val name = prop.simpleName
		val hidden = prop.hasAnnotation<FieldHidden>()
		val censored = prop.hasAnnotation<FieldCensored>()
		NodeField(name.asString(), prop.type.toString(), hidden, censored)
	}.toList()
}
