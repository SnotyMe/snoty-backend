package me.snoty.integration.mail.smtp

import jakarta.mail.Session
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.each
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.mail.MailInput
import org.koin.core.annotation.Single

@RegisterNode(
	name = "smtp",
	displayName = "SMTP",
	settingsType = SmtpSettings::class,
	inputType = MailInput::class,
	position = NodePosition.END,
)
@Single
class SmtpNodeHandler : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: Collection<IntermediateData>): NodeOutput {
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
