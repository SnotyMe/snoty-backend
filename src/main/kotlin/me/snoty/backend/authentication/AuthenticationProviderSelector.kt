package me.snoty.backend.authentication

import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.core.qualifier.StringQualifier

data class AuthConfig(val type: String)

const val AUTHENTICATION = "authentication"

@Single
fun selectAuthenticationProvider(koin: Koin, configLoader: ConfigLoader): AuthenticationAdapter {
	val authConfig = configLoader.load<AuthConfig>(AUTHENTICATION)

	return koin.get(qualifier = StringQualifier(authConfig.type))
}
