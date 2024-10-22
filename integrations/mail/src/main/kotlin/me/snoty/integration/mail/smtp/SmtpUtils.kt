package me.snoty.integration.mail.smtp

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import me.snoty.integration.mail.MailInput

data class SmtpSend(
	val from: String,
	val to: String,
	val mimeType: String,
)

fun createMessage(mail: MailInput, session: Session, settings: SmtpSend): MimeMessage {
	val mimeBodyPart = MimeBodyPart()
	mimeBodyPart.setContent(mail.body, settings.mimeType)

	val multipart = MimeMultipart()
	multipart.addBodyPart(mimeBodyPart)

	return MimeMessage(session).apply {
		setFrom(settings.from)
		setRecipients(Message.RecipientType.TO, settings.to)
		setSubject(mail.subject)
		setContent(multipart)
	}
}

const val TEXT_HTML_UTF8 = "text/html; charset=utf-8"
