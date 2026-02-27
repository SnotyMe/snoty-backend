package me.snoty.backend.authentication

import me.snoty.backend.adapter.Adapter
import org.koin.core.Koin

interface AuthenticationAdapter : Adapter {
	suspend fun buildAuthenticationMetadata(koin: Koin): AuthenticationMetadata

	companion object {
		const val CONFIG_GROUP = "authentication"
	}
}
