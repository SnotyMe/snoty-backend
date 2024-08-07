@file:OptIn(KspExperimental::class)

package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

inline fun <reified T : Any> Resolver.resolveClassFromAnnotation(declaringClass: KSClassDeclaration, argument: KProperty1<T, KClass<out Any>>): KSClassDeclaration {
	val annotation = T::class
	val type = ((
		declaringClass.annotations.toList()
			.single { it.annotationType.resolve().toClassName().canonicalName == annotation.qualifiedName }
			.arguments
			.firstOrNull { it.name!!.asString() == argument.name }
				?: throw IllegalArgumentException("Annotation ${annotation.simpleName} declared on ${declaringClass.qualifiedName!!.asString()} does not have argument $argument")
		).value as KSType).toClassName()

	return getKotlinClassByName(type.canonicalName) ?: throw IllegalStateException("Could not resolve class for $type")
}

inline fun <reified T : Annotation> KSAnnotated.hasAnnotation() = isAnnotationPresent(T::class)
inline fun <reified T : Annotation> KSAnnotated.getAnnotation() = getAnnotationsByType(T::class).firstOrNull()
