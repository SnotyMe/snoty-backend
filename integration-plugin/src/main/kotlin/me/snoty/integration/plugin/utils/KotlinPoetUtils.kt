package me.snoty.integration.plugin.utils

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KProperty

fun PropertySpec.override() = toBuilder()
	.apply {
		modifiers -= KModifier.ABSTRACT
		modifiers += KModifier.OVERRIDE
	}

fun TypeSpec.overrideProperty(property: KProperty<*>) =
	propertySpecs
	.single { it.name == property.name }
	.override()

fun PropertySpec.Builder.removeModifiers(vararg modifiers: KModifier) = apply {
	this.modifiers -= modifiers
}
