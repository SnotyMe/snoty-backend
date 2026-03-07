package me.snoty.backend.authentication.oidc

import kotlinx.serialization.Serializable
import me.snoty.backend.authentication.AuthenticationMetadata

@Serializable
data class OidcAuthenticationMetadata(
    override val adapter: String = OIDC_ADAPTER_TYPE,
    val authUrl: String,
    val clientId: String,
) : AuthenticationMetadata
