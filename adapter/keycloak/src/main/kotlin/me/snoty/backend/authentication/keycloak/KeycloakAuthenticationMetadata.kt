package me.snoty.backend.authentication.keycloak

import kotlinx.serialization.Serializable
import me.snoty.backend.authentication.AuthenticationMetadata

@Serializable
data class KeycloakAuthenticationMetadata(
    override val adapter: String = KEYCLOAK_ADAPTER_TYPE,
    val authUrl: String,
    val tokenUrl: String,
    val logoutUrl: String,
    val clientId: String,
    val publicClientId: String?,
    val providers: List<KeycloakIdentityProvider>,
) : AuthenticationMetadata

@Serializable
data class KeycloakIdentityProvider(
    val id: String,
    val alias: String,
    val displayName: String?,
    val providerId: String,
)
