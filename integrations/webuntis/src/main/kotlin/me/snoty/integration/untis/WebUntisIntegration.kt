package me.snoty.integration.untis

import kotlinx.serialization.Serializable
import me.snoty.integration.common.NodeContext
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

class WebUntisIntegration : NodeHandlerContributor {
	override fun contributeHandlers(registry: NodeRegistry, context: NodeContext) {
		registry.registerIntegrationHandler("webuntis", WebUntisFetcher(context))
	}
}

val UNTIS_CODEC_MODULE = listOf(UntisDateTime.Companion)
