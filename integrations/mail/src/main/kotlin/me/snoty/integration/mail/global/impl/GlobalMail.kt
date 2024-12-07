package me.snoty.integration.mail.global.impl

import me.snoty.backend.config.ConfigWrapper
import me.snoty.integration.mail.MailInput
import me.snoty.integration.mail.global.MailSettings

sealed class GlobalMailConfig

@ConfigWrapper
data class GlobalMailConfigWrapper(val globalMail: GlobalMailConfig)

sealed interface GlobalMailService {
	fun send(mails: Collection<MailInput>, settings: MailSettings)
}
