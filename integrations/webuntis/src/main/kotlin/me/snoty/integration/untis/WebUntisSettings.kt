package me.snoty.integration.untis

import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.wiring.node.NodeSettings

interface WebUntisSettings : NodeSettings {
	val baseUrl: String
	val school: String
	val username: String
	@FieldCensored
	val appSecret: String
}
