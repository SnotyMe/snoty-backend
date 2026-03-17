package me.snoty.backend.authentication.oidc

import io.ktor.openapi.*
import me.snoty.backend.authentication.AuthenticationAdapter
import me.snoty.backend.authentication.Role
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
object OidcKoinModule

const val OIDC_ADAPTER_TYPE = "oidc"

class OidcAdapter : AuthenticationAdapter {
    override val supportedTypes: List<String> = listOf(OIDC_ADAPTER_TYPE)
	override val koinModule: DiModule = OidcKoinModule.module()

	override suspend fun buildAuthenticationMetadata(event: AuthenticationAdapter.OnBuildAuthenticationMetadata): OidcAuthenticationMetadata {
		val oidcConfig = event.koin.get<OidcConfig>()
		return OidcAuthenticationMetadata(
			authUrl = oidcConfig.authUrl,
			logoutUrl = oidcConfig.logoutUrl,
			clientId = oidcConfig.clientId,
		)
	}

	override fun getMetadataJsonSchema(jsonSchemaInference: JsonSchemaInference): JsonSchema =
		jsonSchemaInference.jsonSchema<OidcAuthenticationMetadata>()
}

data class OidcConfig(
	val issuerUrl: String,
	val oidcUrl: String = issuerUrl,
	val authUrl: String = "$oidcUrl/auth",
	val logoutUrl: String = "$oidcUrl/logout",
	val tokenUrl: String = "$oidcUrl/token",
	val certUrl: String = "$oidcUrl/certs",
	val userInfoUrl: String = "$oidcUrl/userinfo",
	val clientId: String,
	val clientSecret: String,
	val rolesClaim: String = "groups", // not standardized in OIDC
	val roleMappings: RoleMapping = mapOf(
		"snoty-admin" to Role.ADMIN.name,
	),
)

/**
 * Key: External Group
 * Value: Snoty Group
 */
typealias RoleMapping = Map<String, String>

@Single
fun provideOidcConfig(configLoader: ConfigLoader): OidcConfig = configLoader.load(AuthenticationAdapter.CONFIG_GROUP)
