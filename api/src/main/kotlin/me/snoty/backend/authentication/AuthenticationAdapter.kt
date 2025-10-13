package me.snoty.backend.authentication

import me.snoty.backend.adapter.Adapter

interface AuthenticationAdapter : Adapter {
	companion object {
		val CONFIG_KEY = "authentication"
	}
}
