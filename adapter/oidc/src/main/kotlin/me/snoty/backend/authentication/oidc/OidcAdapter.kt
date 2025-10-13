package me.snoty.backend.authentication.oidc

import me.snoty.backend.authentication.AuthenticationAdapter
import org.koin.ksp.generated.defaultModule

class OidcAdapter : AuthenticationAdapter {
	override val supportedTypes: List<String> = listOf("oidc")
	override val koinModule = defaultModule
}
