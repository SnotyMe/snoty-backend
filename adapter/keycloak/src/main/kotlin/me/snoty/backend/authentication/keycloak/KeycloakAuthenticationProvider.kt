package me.snoty.backend.authentication.keycloak

import io.ktor.client.*
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.authentication.oidc.OidcAuthenticationProvider
import me.snoty.backend.config.Config
import me.snoty.backend.utils.http.INTERNAL_HTTP_CLIENT
import org.keycloak.admin.client.resource.RealmResource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [AuthenticationProvider::class])
class KeycloakAuthenticationProvider(
	keycloakConfig: KeycloakConfig,
	realm: RealmResource,
	config: Config,
	@Named(INTERNAL_HTTP_CLIENT) httpClient: HttpClient,
) : OidcAuthenticationProvider(
	oidcConfig = keycloakConfig.toOidcConfig(),
	httpClient = httpClient,
	config = config,
) {
	private val users = realm.users()

	override suspend fun getRolesById(userId: String): List<Role> = users.get(userId)
		.groups()
		.map { it.name }
		.mapToSnotyRoles()
}
