package me.snoty.integration.common.utils

import me.snoty.integration.common.IntegrationSettings
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class ExcludeInJobName

@Target(AnnotationTarget.PROPERTY)
annotation class RedactInJobName

@Suppress("UNCHECKED_CAST")
inline fun <reified S : IntegrationSettings> S.formatProperties(): String {
	return this::class.declaredMemberProperties
		.filterNot { prop -> prop.hasAnnotation<ExcludeInJobName>() }
		.joinToString(", ") { prop ->
			val redacted = prop.hasAnnotation<RedactInJobName>()
			val value =
				if (redacted) "<redacted>"
				else (prop as KProperty1<S, *>).get(this@formatProperties)
			"${prop.name}=$value"
		}
}
