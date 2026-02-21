package me.snoty.backend.featureflags

import me.snoty.backend.adapter.Adapter

interface FeatureFlagsAdapter : Adapter {
	companion object {
		const val CONFIG_GROUP = "featureFlags"
	}
}
