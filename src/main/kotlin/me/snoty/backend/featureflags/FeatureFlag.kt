package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import kotlin.reflect.KClass

abstract class FeatureFlag<T>(
	val name: String,
	private val defaultValue: T?
) {
	abstract val getter: Client.(T?) -> T

	fun getValue(client: Client): T {
		return getter(client, defaultValue)
	}
}

class EnumFeatureFlag<E : Enum<E>>(
	name: String,
	defaultValue: E?,
	enumClass: KClass<E>
) : FeatureFlag<E>(name, defaultValue) {
	override val getter: Client.(E?) -> E = { defaultValue ->
		val value = this.getStringValue(name, defaultValue?.name)

		enumClass.java.enumConstants.first { it.name == value }
	}
}
