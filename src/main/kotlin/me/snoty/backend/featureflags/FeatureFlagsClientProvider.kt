package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.OpenFeatureAPI
import org.koin.core.annotation.Single

@Single
fun provideOpenFeatureClient(featureFlagsAdapter: FeatureFlagsAdapter, featureProvider: FeatureProvider): Client {
	val openFeatureAPI = OpenFeatureAPI.getInstance()
	val name = featureFlagsAdapter.supportedTypes.first()
	openFeatureAPI.setProviderAndWait(name, featureProvider)
	return openFeatureAPI.getClient(name)
}
