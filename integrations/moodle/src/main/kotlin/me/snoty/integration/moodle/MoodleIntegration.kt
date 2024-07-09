package me.snoty.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.NodeContextBuilder
import me.snoty.integration.common.utils.RedactInJobName
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.registerIntegrationHandler

@Serializable
data class MoodleSettings(
	val baseUrl: String,
	val username: String,
	@RedactInJobName
	val appSecret: String,
) : NodeSettings

class MoodleNodeHandlerContributor : NodeHandlerContributor {
	override fun contributeHandlers(registry: NodeRegistry, nodeContextBuilder: NodeContextBuilder) {
		registry.registerIntegrationHandler("moodle", nodeContextBuilder) { context ->
			MoodleFetcher(context)
		}
	}
}
