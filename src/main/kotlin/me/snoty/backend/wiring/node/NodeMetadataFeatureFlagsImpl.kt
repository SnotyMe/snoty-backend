package me.snoty.backend.wiring.node

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import me.snoty.backend.utils.ifDev
import me.snoty.backend.utils.otherwise
import me.snoty.integration.common.wiring.node.template.NodeMetadataFeatureFlags
import org.koin.core.annotation.Single

@Single(binds = [NodeMetadataFeatureFlags::class])
class NodeMetadataFeatureFlagsImpl(override val client: Client, environment: Environment) : NodeMetadataFeatureFlags, FeatureFlagsContainer {
	override val cacheNodeMetadata by FeatureFlagBoolean(
		"node.cacheNodeMetadata",
		environment ifDev { false } otherwise { true }
	)
	override val cacheNodeTemplates by FeatureFlagBoolean(
		"node.cacheNodeTemplates",
		environment ifDev { false } otherwise { true }
	)
}
