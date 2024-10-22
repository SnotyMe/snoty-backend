package me.snoty.integration.mail.global.impl

import jakarta.mail.Session
import me.snoty.integration.mail.MailInput
import me.snoty.integration.mail.global.MailSettings
import me.snoty.integration.mail.smtp.SmtpSend
import me.snoty.integration.mail.smtp.createMessage
import java.util.*


data class Smtp(
	val host: String,
	val port: Int,
	val startTls: Boolean = false,
	val username: String,
	val password: String,
	val from: String,
) : GlobalMailConfig()

fun Smtp.toConfiguration(mailSettings: MailSettings) = Properties().apply {
	put("mail.smtp.auth", "true")
	put("mail.smtp.starttls.enable", startTls)
	put("mail.smtp.host", host)
	put("mail.smtp.port", port)
	put("mail.smtp.user", username)
	put("mail.smtp.password", password)
	put("mail.smtp.from", from)
	put("mail.smtp.to", mailSettings.to)
}

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
