package me.snoty.integration.plugin.utils

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

fun PropertySpec.override() = toBuilder()
	.apply {
		modifiers -= KModifier.ABSTRACT
		modifiers += KModifier.OVERRIDE
	}
