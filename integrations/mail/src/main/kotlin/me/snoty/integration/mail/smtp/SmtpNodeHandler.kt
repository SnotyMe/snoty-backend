package me.snoty.integration.mail.smtp

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.each
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.Subsystem
import me.snoty.integration.mail.MailInput
import org.koin.core.annotation.Single


@RegisterNode(
	subsystem = Subsystem.INTEGRATION,
	displayName = "SMTP",
	type = "smtp",
	settingsType = SmtpSettings::class,
	inputType = MailInput::class,
	position = NodePosition.END,
)
@Single
class SmtpNodeHandler : NodeHandler {
	override suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>): NodeOutput {
		val settings = node.settings as SmtpSettings
		val properties = settings.toConfiguration()
		val session = Session.getInstance(properties)
		return session.getTransport("smtp").use { transport ->
			transport.connect(settings.host, settings.username, settings.password)

			each<MailInput>(input) { mail ->
				val message = createMessage(mail, session, settings)
				transport.sendMessage(message, message.allRecipients)
			}
		}
	}

	private fun createMessage(mail: MailInput, session: Session, settings: SmtpSettings): MimeMessage {
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
}
