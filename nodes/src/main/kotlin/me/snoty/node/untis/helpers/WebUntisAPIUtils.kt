package me.snoty.node.untis.helpers

import me.snoty.node.untis.WebUntisSettings
import me.snoty.node.untis.auth.UntisAuthentication
import me.snoty.node.untis.param.UserDataParams

fun WebUntisSettings.toUserParams() =
	UserDataParams(
		auth = UntisAuthentication.createAuthObject(
			username,
			appSecret
		)
	)
