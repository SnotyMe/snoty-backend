package me.snoty.integration.mail.global

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldHidden
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.mail.smtp.TEXT_HTML_UTF8

@Serializable
data class MailSettings(
	override val name: String,
	val to: String,
	@FieldHidden
	@FieldDefaultValue(TEXT_HTML_UTF8)
	val mimeType: String = TEXT_HTML_UTF8,
) : NodeSettings
