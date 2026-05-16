package me.snoty.backend.authentication.oidc

import kotlinx.serialization.Serializable
import me.snoty.backend.authentication.AuthenticationMetadata

@Serializable
data class OidcAuthenticationMetadata(
    override val adapter: String = OIDC_ADAPTER_TYPE,
    val authUrl: String,
    val tokenUrl: String,
    val logoutUrl: String,
    val clientId: String,
    val publicClientId: String?,
) : AuthenticationMetadata
