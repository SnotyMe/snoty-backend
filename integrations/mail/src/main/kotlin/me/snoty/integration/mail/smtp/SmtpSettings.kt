package me.snoty.integration.mail.smtp

import kotlinx.serialization.Serializable
import me.snoty.backend.config.toProperties
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldHidden
import me.snoty.integration.common.model.metadata.FieldName
import me.snoty.integration.common.wiring.node.NodeSettings

@Serializable
data class SmtpSettings(
	override val name: String = "SMTP",
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
