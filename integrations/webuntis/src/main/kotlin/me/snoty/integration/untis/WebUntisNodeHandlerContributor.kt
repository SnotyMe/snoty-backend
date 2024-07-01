package me.snoty.integration.untis

import kotlinx.serialization.Serializable
import me.snoty.integration.common.NodeContextBuilder
import me.snoty.integration.common.utils.RedactInJobName
import me.snoty.integration.common.wiring.node.NodeHandlerContributor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.registerIntegrationHandler
import me.snoty.integration.untis.model.UntisDateTime

@Serializable
data class WebUntisSettings(
	val baseUrl: String,
	val school: String,
	val username: String,
	@RedactInJobName
	val appSecret: String,
) : NodeSettings

class WebUntisNodeHandlerContributor : NodeHandlerContributor {
	override fun contributeHandlers(registry: NodeRegistry, nodeContextBuilder: NodeContextBuilder) {
		registry.registerIntegrationHandler("webuntis", nodeContextBuilder) { context ->
			WebUntisFetcher(context)
		}
	}
}

val UNTIS_CODEC_MODULE = listOf(UntisDateTime.Companion)
