package me.snoty.backend.utils

import kotlin.reflect.KClass

/**
 * Returns a name resolvable by [Class.forName] for the given [clazz].
 */
fun resolveClassName(clazz: KClass<*>?): String {
	if (clazz == null) return "null"
	val type = ClassUtils.boxOrGetType(clazz.java)
	return type.canonicalName
}
