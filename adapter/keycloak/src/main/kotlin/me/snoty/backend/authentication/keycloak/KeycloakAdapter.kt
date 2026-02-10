package me.snoty.backend.authentication.keycloak

import me.snoty.backend.authentication.AuthenticationAdapter
import me.snoty.backend.authentication.Role
import me.snoty.backend.authentication.oidc.OidcConfig
import me.snoty.backend.authentication.oidc.RoleMapping
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

const val KEYCLOAK_ADAPTER_TYPE = "keycloak"
private const val CONFIG_KEY = "${AuthenticationAdapter.CONFIG_GROUP}.${KEYCLOAK_ADAPTER_TYPE}"

@Module
@ComponentScan
object KeycloakKoinModule

class KeycloakAdapter : AuthenticationAdapter {
	override val supportedTypes: List<String> = listOf(KEYCLOAK_ADAPTER_TYPE)
	override val koinModule = KeycloakKoinModule.module()
}

data class KeycloakConfig(
	val baseUrl: String,
	val realm: String,
	val issuerUrl: String = "$baseUrl/realms/$realm", // override when using internal networking
	val clientId: String,
	val clientSecret: String,
	val roleMappings: RoleMapping = mapOf(
		"snoty-admin" to Role.ADMIN.name,
	),
)

fun KeycloakConfig.toOidcConfig() = OidcConfig(
	issuerUrl = issuerUrl,
	oidcUrl = "$baseUrl/realms/$realm/protocol/openid-connect",
	clientId = clientId,
	clientSecret = clientSecret,
	roleMappings = roleMappings,
)

@Single
fun provideKeycloakConfig(configLoader: ConfigLoader): KeycloakConfig = configLoader.load(CONFIG_KEY)
