package me.snoty.backend.authentication.keycloak

import me.snoty.backend.authentication.AuthenticationAdapter
import org.koin.ksp.generated.defaultModule

class KeycloakAdapter : AuthenticationAdapter {
	override val supportedTypes: List<String> = listOf("keycloak")
	override val koinModule = defaultModule
}
