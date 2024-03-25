package me.snoty.backend.integration.untis.helpers

import me.snoty.backend.integration.untis.WebUntisSettings
import me.snoty.backend.integration.untis.auth.UntisAuthentication
import me.snoty.backend.integration.untis.param.UserDataParams

fun WebUntisSettings.toUserParams() =
	UserDataParams(auth = UntisAuthentication.createAuthObject(username, appSecret))
