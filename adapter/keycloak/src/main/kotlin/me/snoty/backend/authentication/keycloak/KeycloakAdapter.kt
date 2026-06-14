package me.snoty.backend.authentication.keycloak

import io.ktor.openapi.*
import me.snoty.backend.authentication.AuthenticationAdapter
import me.snoty.backend.authentication.Role
import me.snoty.backend.authentication.oidc.OidcConfig
import me.snoty.backend.authentication.oidc.RoleMapping
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.keycloak.admin.client.resource.RealmResource
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

    override suspend fun buildAuthenticationMetadata(event: AuthenticationAdapter.OnBuildAuthenticationMetadata): KeycloakAuthenticationMetadata {
        val keycloakConfig = event.koin.get<KeycloakConfig>()
        val oidcConfig = keycloakConfig.toOidcConfig(keycloakConfig.baseUrl)
        val realm: RealmResource = event.koin.get()

        val providers = realm.identityProviders().findAll()
            .map {
                KeycloakIdentityProvider(
                    id = it.internalId,
                    alias = it.alias,
                    displayName = it.displayName,
                    providerId = it.providerId,
                )
            }

        return KeycloakAuthenticationMetadata(
            authUrl = oidcConfig.authUrl,
            tokenUrl = oidcConfig.tokenUrl,
            logoutUrl = oidcConfig.logoutUrl,
            clientId = oidcConfig.clientId,
            publicClientId = oidcConfig.publicClientId,
            providers = providers,
        )
    }

    override fun getMetadataJsonSchema(jsonSchemaInference: JsonSchemaInference): JsonSchema =
        jsonSchemaInference.jsonSchema<KeycloakAuthenticationMetadata>()
}

data class KeycloakConfig(
    val baseUrl: String,
    /**
     * Internal Base URL used for communication between the backend and Keycloak.
     * This reduces the amount of networking overhead whilst preserving the public base URL
     * that is referenced in the public-facing authentication metadata.
     */
    val internalBaseUrl: String = baseUrl,
    val realm: String,
    val issuerUrl: String = "$baseUrl/realms/$realm", // override when using internal networking
    val clientId: String,
    val publicClientId: String? = null,
    val clientSecret: String,
    val roleMappings: RoleMapping = mapOf(
        "snoty-admin" to Role.ADMIN.name,
    ),
)

fun KeycloakConfig.toOidcConfig(keycloakBaseUrl: String) = OidcConfig(
    issuerUrl = issuerUrl,
    oidcUrl = "$keycloakBaseUrl/realms/$realm/protocol/openid-connect",
    clientId = clientId,
    publicClientId = publicClientId,
    clientSecret = clientSecret,
    roleMappings = roleMappings,
)

@Single
fun provideKeycloakConfig(configLoader: ConfigLoader): KeycloakConfig = configLoader.load(CONFIG_KEY)
