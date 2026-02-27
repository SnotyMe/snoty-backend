package me.snoty.backend.authentication.oidc

import me.snoty.backend.authentication.AuthenticationMetadata

data class OidcAuthenticationMetadata(
    override val adapter: String = OIDC_ADAPTER_TYPE,
    val authUrl: String,
    val clientId: String,
) : AuthenticationMetadata
