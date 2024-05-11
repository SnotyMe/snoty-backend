package me.snoty.integration.untis.helpers

import me.snoty.integration.untis.WebUntisSettings
import me.snoty.integration.untis.auth.UntisAuthentication
import me.snoty.integration.untis.param.UserDataParams

fun WebUntisSettings.toUserParams() =
	UserDataParams(auth = UntisAuthentication.createAuthObject(username, appSecret))
