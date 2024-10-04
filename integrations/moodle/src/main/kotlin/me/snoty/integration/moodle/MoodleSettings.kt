package me.snoty.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldDescription
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
	@FieldDefaultValue("false")
	@FieldDescription("Whether to emit 'done' assignments (may break auto deletions on assignment completion)")
	val emitDoneAssignments: Boolean = false,
	@FieldDefaultValue("true")
	@FieldDescription("Whether to emit 'closed' assignments (you cannot submit anything)")
	val emitClosedAssignments: Boolean = true,
) : NodeSettings
