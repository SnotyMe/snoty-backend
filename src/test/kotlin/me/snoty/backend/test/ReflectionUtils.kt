package me.snoty.backend.test

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
fun <T> Any.getField(name: String)
	= this::class.declaredMemberProperties.find { it.name == name }!!
		.run {
			isAccessible = true
			(this as KProperty1<Any, T>).get(this@getField)
		}
