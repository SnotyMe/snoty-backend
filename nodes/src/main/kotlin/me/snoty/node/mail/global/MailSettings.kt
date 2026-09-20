package me.snoty.node.mail.global

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.FieldDefaultValue
import me.snoty.backend.schema.FieldHidden
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.node.mail.smtp.TEXT_HTML_UTF8

@Serializable
data class MailSettings(
	val to: String,
	@FieldHidden
	@FieldDefaultValue(TEXT_HTML_UTF8)
	val mimeType: String = TEXT_HTML_UTF8,
) : NodeSettings
