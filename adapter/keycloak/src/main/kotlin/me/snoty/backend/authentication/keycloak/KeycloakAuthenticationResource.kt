package me.snoty.backend.authentication.keycloak

import io.ktor.client.*
import io.ktor.server.auth.*
import me.snoty.backend.authentication.oidc.OidcConfig
import me.snoty.backend.authentication.oidc.oidcAuthenticationResource
import me.snoty.backend.utils.http.INTERNAL_HTTP_CLIENT
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("keycloakAuthentication")
fun keycloakAuthenticationResource(
    authConfig: OidcConfig,
    @Named(INTERNAL_HTTP_CLIENT) httpClient: HttpClient,
    serverSettings: OAuthServerSettings.OAuth2ServerSettings,
) =
    oidcAuthenticationResource(authConfig, httpClient, serverSettings)
