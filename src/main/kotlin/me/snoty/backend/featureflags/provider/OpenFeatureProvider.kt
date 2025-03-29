package me.snoty.backend.featureflags.provider

import dev.openfeature.sdk.Client
import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.OpenFeatureAPI
import me.snoty.backend.config.ProviderFeatureFlagConfig
import kotlin.reflect.KClass

interface OpenFeatureProvider<C : ProviderFeatureFlagConfig> {
	val configClass: KClass<C>
	
	fun createClient(config: C): Client
}

abstract class BaseOpenFeatureProvider<C : ProviderFeatureFlagConfig>(override val configClass: KClass<C>) : OpenFeatureProvider<C> {
	abstract val name: String

	override fun createClient(config: C): Client {
		val provider = createFeatureProvider(config)

		val openFeatureAPI = OpenFeatureAPI.getInstance()
		openFeatureAPI.setProviderAndWait(name, provider)
		return openFeatureAPI.getClient(name)
	}

	protected abstract fun createFeatureProvider(config: C): FeatureProvider
}
