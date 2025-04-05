package me.snoty.integration.mail.global.impl

import jakarta.mail.Session
import me.snoty.backend.config.toProperties
import me.snoty.integration.mail.MailInput
import me.snoty.integration.mail.global.MailSettings
import me.snoty.integration.mail.smtp.SmtpSend
import me.snoty.integration.mail.smtp.createMessage

data class Smtp(
	val host: String,
	val port: Int,
	val startTls: Boolean = false,
	val username: String,
	val password: String,
	val from: String,
) : GlobalMailConfig()

fun Smtp.toConfiguration(mailSettings: MailSettings) = mapOf(
	"mail.smtp.auth" to "true",
	"mail.smtp.starttls.enable" to startTls,
	"mail.smtp.host" to host,
	"mail.smtp.port" to port,
	"mail.smtp.user" to username,
	"mail.smtp.password" to password,
	"mail.smtp.from" to from,
	"mail.smtp.to" to mailSettings.to,
).toProperties()

class SmtpGlobalMailService(private val config: Smtp) : GlobalMailService {
	override fun send(mails: Collection<MailInput>, settings: MailSettings) {
		val properties = config.toConfiguration(settings)
		val session = Session.getInstance(properties)
		val smtpSend = SmtpSend(
			from = config.from,
			to = settings.to,
			mimeType = settings.mimeType
		)

		session.getTransport("smtp").use { transport ->
			transport.connect(config.host, config.username, config.password)

			mails.map { mail ->
				createMessage(mail, session, smtpSend)
			}.forEach { message ->
				transport.sendMessage(message, message.allRecipients)
			}
		}
	}
}
