package me.snoty.backend.authentication

import me.snoty.backend.adapter.Adapter

interface AuthenticationAdapter : Adapter {
	companion object {
		const val CONFIG_GROUP = "authentication"
	}
}
