package me.snoty.node.notion.page

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.schema.FieldDefaultValue
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.schema.FieldHidden
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.node.notion.model.NotionParent

@Serializable
data class NotionPageSettings(
	val parent: NotionParent,
	@FieldDescription("Whether to archive pages when deletions are detected.")
	@FieldDefaultValue("true")
	val archiveOnDeletion: Boolean = true,

	@FieldCensored
	@FieldHidden
	val token: String,
) : NodeSettings
