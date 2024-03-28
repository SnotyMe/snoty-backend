package me.snoty.backend.dev.auth

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.Masked

/**
 * Configuration that resembles the environment variables for the Keycloak container.
 * The same .env file can be used for the keycloak container and the application.
 * This reduces the risk of configuration drift between the two.
 */
data class KeycloakContainerConfig(
	@ConfigAlias("KEYCLOAK_ADMIN")
	val adminUser: String,
	@ConfigAlias("KEYCLOAK_ADMIN_PASSWORD")
	val adminPassword: Masked,
	@ConfigAlias("KEYCLOAK_PORT")
	val port: Int = 8081
)
