package me.snoty.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldName
import me.snoty.integration.common.wiring.node.NodeSettings

@Serializable
data class MoodleSettings(
	override val name: String = "Moodle",
	@FieldName("Base URL")
	val baseUrl: String,
	val username: String,
	@FieldCensored
	val appSecret: String,
) : NodeSettings
