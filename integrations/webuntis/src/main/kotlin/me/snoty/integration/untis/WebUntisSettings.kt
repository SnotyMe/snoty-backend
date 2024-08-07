package me.snoty.integration.untis

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.untis.model.UntisDateTime

@Serializable
data class WebUntisSettings(
	override val name: String = "WebUntis",
	val baseUrl: String,
	val school: String,
	val username: String,
	@FieldCensored
	val appSecret: String,
) : NodeSettings

val UNTIS_CODEC_MODULE = listOf(UntisDateTime.Companion)
