package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client

interface FeatureFlagsContainer {
	val client: Client

	operator fun <T> FeatureFlag<T>.getValue(thisRef: FeatureFlagsContainer, property: Any?): T = getValue(thisRef.client)
}
