package me.snoty.backend.utils

import com.sksamuel.hoplite.simpleName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

/**
 * Returns a name resolvable by [Class.forName] for the given [clazz].
 */
fun resolveClassName(clazz: KClass<*>?): String {
	if (clazz == null) return "null"
	val type = ClassUtils.boxOrGetType(clazz.java)
	return type.canonicalName
}

val KType.simpleClassName
	get() = this.jvmErasure.simpleName ?: this.simpleName.substringAfter("class ")
