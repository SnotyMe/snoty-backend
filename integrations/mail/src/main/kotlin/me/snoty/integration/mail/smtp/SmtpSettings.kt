package me.snoty.integration.mail.smtp

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldHidden
import me.snoty.integration.common.model.metadata.FieldName
import me.snoty.integration.common.wiring.node.NodeSettings
import java.util.Properties

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

fun SmtpSettings.toConfiguration() = Properties().apply {
	put("mail.smtp.auth", "true")
	put("mail.smtp.starttls.enable", startTls)
	put("mail.smtp.host", host)
	put("mail.smtp.port", port)
	put("mail.smtp.user", username)
	put("mail.smtp.password", password)
	put("mail.smtp.from", from)
	put("mail.smtp.to", to)
}
