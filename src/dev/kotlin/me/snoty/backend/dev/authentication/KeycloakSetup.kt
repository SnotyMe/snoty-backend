package me.snoty.backend.dev.authentication

import com.sksamuel.hoplite.Masked
import me.snoty.backend.config.loadContainerConfig
import me.snoty.backend.dev.spi.DevRunnable
import org.keycloak.admin.client.KeycloakBuilder

const val REALM_NAME = "snoty"
const val ADMIN_CLI = "admin-cli"

class KeycloakSetup : DevRunnable() {
	override fun run() {
		val containerConfig = loadContainerConfig<KeycloakContainerConfig>("keycloak")
			.getUnsafe()
		val serverUrl = "http://localhost:${containerConfig.port}"

		val result = setup(serverUrl, containerConfig)

		System.setProperty("config.override.authentication.baseUrl", serverUrl)
		System.setProperty("config.override.authentication.realm", REALM_NAME)
		System.setProperty("config.override.authentication.clientId", result.clientId)
		System.setProperty("config.override.authentication.clientSecret", result.clientSecret)
	}
}

fun setup(serverUrl: String, config: KeycloakContainerConfig, vararg extraRedirectUrls: String): KeycloakConfigurationResult {
	val keycloak = KeycloakBuilder.builder()
		.serverUrl(serverUrl)
		// the realm of the user we're authenticating with
		.realm("master")
		.username(config.adminUser)
		.password(config.adminPassword.value)
		.clientId(ADMIN_CLI)
		.build()

	val result = KeycloakConfigurer(keycloak.realms(), REALM_NAME)
		.configure(*extraRedirectUrls)

	return result
}

fun main() {
	print("Keycloak URL: ")
	val keycloakUrl = readln()

	print("Backend URL: ")
	val backendUrl = readln()

	print("Admin user: ")
	val adminUser = readln()

	print("Admin password: ")
	val adminPassword = readln()
	val config = KeycloakContainerConfig(adminUser, Masked(adminPassword))

	val result = setup(keycloakUrl, config, "$backendUrl/*")
	println("Result: $result")
}
