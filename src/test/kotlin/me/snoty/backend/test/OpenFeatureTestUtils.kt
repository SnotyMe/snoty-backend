package me.snoty.backend.test

import dev.openfeature.sdk.*
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.Config
import me.snoty.backend.featureflags.FeatureFlag
import me.snoty.backend.featureflags.FeatureFlags
import java.util.concurrent.atomic.AtomicInteger

class OpenFeatureTestProvider : FeatureProvider {
	private val logger = KotlinLogging.logger {}

	override fun getMetadata() = Metadata {
		"testProvider"
	}

	private val flagValues = mutableMapOf<String, Any>()

	fun <T: Any> setFlagValue(flag: FeatureFlag<T>, value: T) {
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

fun testFeatureFlags(config: Config = TestConfig): FeatureFlagsAndProvider {
	val provider = OpenFeatureTestProvider()
	val id = openFeatureClientId.getAndIncrement()
	val openFeature = OpenFeatureAPI.getInstance()
	val clientName = "test-$id"
	openFeature.setProvider(clientName, provider)
	return FeatureFlagsAndProvider(
		FeatureFlags(config, openFeature.getClient(clientName)),
		provider
	)
}

data class FeatureFlagsAndProvider(
	val flags: FeatureFlags,
	val provider: OpenFeatureTestProvider
)
