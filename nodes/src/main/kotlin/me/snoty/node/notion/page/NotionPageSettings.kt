package me.snoty.node.notion.page

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.FieldHidden
import me.snoty.integration.common.wiring.node.NodeSettings
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
