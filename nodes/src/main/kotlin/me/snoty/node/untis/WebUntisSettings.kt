package me.snoty.node.untis

import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.wiring.node.NodeSettings

interface WebUntisSettings : NodeSettings {
	val baseUrl: String
	val school: String
	val username: String
	@FieldCensored
	val appSecret: String
}
