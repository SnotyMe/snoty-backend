package me.snoty.integration.mail.global

import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.mail.MailInput
import me.snoty.integration.mail.global.impl.GlobalMailConfigWrapper
import me.snoty.integration.mail.global.impl.GlobalMailService
import me.snoty.integration.mail.global.impl.Smtp
import me.snoty.integration.mail.global.impl.SmtpGlobalMailService
import org.koin.core.annotation.Single

@Single
@RegisterNode(
	name = "mail",
	displayName = "E-Mail",
	position = NodePosition.END,
	inputType = MailInput::class,
	settingsType = MailSettings::class,
)
class MailNodeHandler(private val mailService: GlobalMailService) : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>
	): NodeOutput {
		val settings: MailSettings = node.getConfig()

		val mails: List<MailInput> = input.map {
			get(it)
		}

		mailService.send(mails, settings)

		return emptyList()
	}
}

@Single
fun getGlobalMailImpl(configLoader: ConfigLoader): GlobalMailService {
	val config = configLoader.load<GlobalMailConfigWrapper>(prefix = null).globalMail
	return when (config) {
		is Smtp -> SmtpGlobalMailService(config)
	}
}
