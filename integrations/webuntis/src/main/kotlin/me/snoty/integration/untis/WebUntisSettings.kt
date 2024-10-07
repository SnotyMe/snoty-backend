package me.snoty.integration.untis

import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.untis.model.UntisDateTime

interface WebUntisSettings : NodeSettings {
	override val name: String
	val baseUrl: String
	val school: String
	val username: String
	@FieldCensored
	val appSecret: String
}

val UNTIS_CODEC_MODULE = listOf(UntisDateTime.Companion)
