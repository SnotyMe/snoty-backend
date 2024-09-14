package me.snoty.backend.test

import dev.openfeature.sdk.*
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.featureflags.FeatureFlag
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

class OpenFeatureTestProvider : FeatureProvider {
	private val logger = KotlinLogging.logger {}

	override fun getMetadata() = Metadata {
		"testProvider"
	}

	private val flagValues = mutableMapOf<String, Any>()

	fun <T : Any> setFlagValue(property: KProperty0<Any>, value: T) {
		property.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		val flag = property.getDelegate() as FeatureFlag<T>
		flagValues[flag.name] = value
	}

	override fun getBooleanEvaluation(key: String, defaultValue: Boolean, ctx: EvaluationContext): ProviderEvaluation<Boolean>
		= evaluate(key, defaultValue)

	override fun getStringEvaluation(key: String, defaultValue: String?, ctx: EvaluationContext): ProviderEvaluation<String>
		= evaluate(key, defaultValue)

	override fun getIntegerEvaluation(key: String, defaultValue: Int?, ctx: EvaluationContext): ProviderEvaluation<Int>
		= evaluate(key, defaultValue)

	override fun getDoubleEvaluation(key: String, defaultValue: Double?, ctx: EvaluationContext): ProviderEvaluation<Double>
		= evaluate(key, defaultValue)

	override fun getObjectEvaluation(key: String, defaultValue: Value?, ctx: EvaluationContext): ProviderEvaluation<Value>
		= evaluate(key, defaultValue)

	private fun <T> evaluate(key: String, defaultValue: T?): ProviderEvaluation<T> {
		@Suppress("UNCHECKED_CAST")
		val value = flagValues[key] as? T? ?: defaultValue
		logger.info { "Evaluated flag $key to $value" }
		return ProviderEvaluation.builder<T>()
			.value(value)
			.build()
	}
}

private val openFeatureClientId = AtomicInteger()

fun testFeatureFlags(): ClientAndProvider {
	val provider = OpenFeatureTestProvider()
	val id = openFeatureClientId.getAndIncrement()
	val openFeature = OpenFeatureAPI.getInstance()
	val clientName = "test-$id"
	openFeature.setProvider(clientName, provider)
	return ClientAndProvider(
		openFeature.getClient(clientName),
		provider,
	)
}

data class ClientAndProvider(
	val client: Client,
	val provider: OpenFeatureTestProvider
)
