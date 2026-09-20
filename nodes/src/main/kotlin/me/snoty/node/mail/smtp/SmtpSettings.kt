package me.snoty.node.mail.smtp

import kotlinx.serialization.Serializable
import me.snoty.backend.config.toProperties
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.schema.FieldDefaultValue
import me.snoty.backend.schema.FieldHidden
import me.snoty.backend.schema.FieldName
import me.snoty.backend.wiring.node.NodeSettings

@Serializable
data class SmtpSettings(
	val host: String,
	val port: Int,
	@FieldName("STARTTLS")
	val startTls: Boolean = false,
	val username: String,
	@FieldCensored
	val password: String,
	val from: String,
	val to: String,
	@FieldHidden
	@FieldDefaultValue(TEXT_HTML_UTF8)
	val mimeType: String = TEXT_HTML_UTF8,
) : NodeSettings

fun SmtpSettings.toConfiguration() = mapOf(
	"mail.smtp.auth" to "true",
	"mail.smtp.starttls.enable" to startTls,
	"mail.smtp.host" to host,
	"mail.smtp.port" to port,
	"mail.smtp.user" to username,
	"mail.smtp.password" to password,
	"mail.smtp.from" to from,
	"mail.smtp.to" to to,
).toProperties()
