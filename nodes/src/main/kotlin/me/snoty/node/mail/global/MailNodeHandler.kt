package me.snoty.node.mail.global

import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.get
import me.snoty.backend.wiring.node.Icon
import me.snoty.backend.wiring.node.NodeHandleContext
import me.snoty.backend.wiring.node.NodeHandler
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import me.snoty.node.mail.MailInput
import me.snoty.node.mail.global.impl.GlobalMailConfigWrapper
import me.snoty.node.mail.global.impl.GlobalMailService
import me.snoty.node.mail.global.impl.Smtp
import me.snoty.node.mail.global.impl.SmtpGlobalMailService
import org.koin.core.annotation.Single

@Single
@RegisterNode(
	name = "mail",
	displayName = "E-Mail",
	icon = Icon(name = "lucide-mail"),
	stereotype = NodeStereotype.END,
	inputType = MailInput::class,
	settingsType = MailSettings::class,
)
class MailNodeHandler(private val mailService: GlobalMailService) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: Collection<IntermediateData>
	): NodeOutput {
		val settings: MailSettings = node.getConfig()

		val mails: List<MailInput> = input.map {
			it.get()
		}

		mailService.send(mails, settings)

		return emptyList()
	}
}

@Single
fun getGlobalMailImpl(configLoader: ConfigLoader): GlobalMailService =
	when (
		val config = configLoader.load<GlobalMailConfigWrapper>(prefix = null).globalMail
	) {
		is Smtp -> SmtpGlobalMailService(config)
	}
