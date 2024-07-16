package me.snoty.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.wiring.node.NodeSettings

@Serializable
data class MoodleSettings(
	override val name: String = "Moodle",
	val baseUrl: String,
	val username: String,
	@FieldCensored
	val appSecret: String,
) : NodeSettings
