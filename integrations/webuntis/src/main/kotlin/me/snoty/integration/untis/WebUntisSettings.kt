package me.snoty.integration.untis

import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.wiring.node.NodeSettings

interface WebUntisSettings : NodeSettings {
	override val name: String
	val baseUrl: String
	val school: String
	val username: String
	@FieldCensored
	val appSecret: String
}
