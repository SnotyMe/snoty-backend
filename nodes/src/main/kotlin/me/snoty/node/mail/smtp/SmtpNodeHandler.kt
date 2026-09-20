package me.snoty.node.mail.smtp

import jakarta.mail.Session
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.each
import me.snoty.backend.wiring.node.Icon
import me.snoty.backend.wiring.node.NodeHandleContext
import me.snoty.backend.wiring.node.NodeHandler
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.node.mail.MailInput
import org.koin.core.annotation.Single

@RegisterNode(
	name = "smtp",
	displayName = "SMTP",
	icon = Icon(name = "lucide-mail"),
	settingsType = SmtpSettings::class,
	inputType = MailInput::class,
	stereotype = NodeStereotype.END,
)
@Single
class SmtpNodeHandler : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: Collection<IntermediateData>): NodeOutput {
		val settings = node.settings as SmtpSettings
		val properties = settings.toConfiguration()
		val session = Session.getInstance(properties)
		return session.getTransport("smtp").use { transport ->
			transport.connect(settings.host, settings.username, settings.password)

			each<MailInput>(input) { mail ->
				val send = SmtpSend(
					from = settings.from,
					to = settings.to,
					mimeType = settings.mimeType,
				)
				val message = createMessage(mail, session, send)
				transport.sendMessage(message, message.allRecipients)
			}
		}
	}
}
