package me.snoty.backend.authentication.keycloak

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.KeycloakBuilder
import org.koin.core.annotation.Single

@Single
fun provideKeycloakApi(config: KeycloakConfig) = KeycloakBuilder.builder()
	.serverUrl(config.baseUrl)
	// the realm of the user we're authenticating with
	.realm(config.realm)
	.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
	.clientId(config.clientId)
	.clientSecret(config.clientSecret)
	.build()
	.realm(config.realm)
	?: error("Failed to build Keycloak client for realm ${config.realm}")
