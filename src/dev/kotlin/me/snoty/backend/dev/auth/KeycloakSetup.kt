package me.snoty.backend.dev.auth

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.parsers.PropsParser
import me.snoty.backend.spi.DevRunnable
import org.keycloak.admin.client.KeycloakBuilder

const val REALM_NAME = "snoty"
const val ADMIN_CLI = "admin-cli"

class KeycloakSetup : DevRunnable() {
	override fun run() {
		val containerConfig = ConfigLoaderBuilder.default()
			.addParser("env", PropsParser())
			// `.env.default` file - WARNING: this assumes all *.default files are .env files
			.addParser("default", PropsParser())
			// local configuration takes precedence
			.addFileSource("infra/keycloak/.env", optional = true, allowEmpty = false)
			.addFileSource("infra/keycloak/.env.default", optional = true, allowEmpty = false)
			.build()
			.loadConfig<KeycloakContainerConfig>()
			.onFailure { logger.warn { "Failed to load KeycloakContainerConfig: ${it.description()}" } }
			.also {
				logger.info { "Loaded KeycloakContainerConfig: $it" }
			}.getUnsafe()
		val serverUrl = "http://localhost:${containerConfig.port}"

		val keycloak = KeycloakBuilder.builder()
			.serverUrl(serverUrl)
			// the realm of the user we're authenticating with
			.realm("master")
			.username(containerConfig.adminUser)
			.password(containerConfig.adminPassword.value)
			.clientId(ADMIN_CLI)
			.build()

		val result = KeycloakConfigurer(keycloak.realms(), REALM_NAME)
			.configure()

		System.setProperty("config.override.authentication.serverUrl", "$serverUrl/realms/$REALM_NAME")
		System.setProperty("config.override.authentication.clientId", result.clientId)
		System.setProperty("config.override.authentication.clientSecret", result.clientSecret)
	}
}
