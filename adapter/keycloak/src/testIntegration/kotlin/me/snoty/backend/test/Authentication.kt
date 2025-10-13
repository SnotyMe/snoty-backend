package me.snoty.backend.test

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.snoty.backend.authentication.oidc.OidcConfig
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation


private val httpClient = HttpClient {
	install(ContentNegotiation) {
		json(Json {
			ignoreUnknownKeys = true
		})
	}
}
suspend fun getAccessToken(oidcConfig: OidcConfig, username: String, password: String): String {
	@Serializable
	data class TokenResponse(@SerialName("access_token") val accessToken: String)

	return httpClient.submitForm(
		url = "${oidcConfig.oidcUrl}/token",
		formParameters = parameters {
			append("username", username)
			append("password", password)
			append("grant_type", "password")
			append("client_id", oidcConfig.clientId)
			append("client_secret", oidcConfig.clientSecret)
		}
	).body<TokenResponse>().accessToken
}

data class UserLogin(val properties: UserRepresentation, val password: String, val accessToken: String)

suspend fun RealmResource.createAndLoginUser(
	oidcConfig: OidcConfig,
	firstName: String = "First",
	lastName: String = "Last",
	username: String = "${firstName.lowercase()}.${lastName.lowercase()}",
	password: String = "12345",
	email: String = "$username@test.snoty.me"
) = createAndLoginUser(oidcConfig) {
	this.username = username
	this.firstName = firstName
	this.lastName = lastName
	this.email = email
	CredentialRepresentation().apply {
		isTemporary = false
		type = CredentialRepresentation.PASSWORD
		value = password
	}
}

suspend fun RealmResource.createAndLoginUser(
	oidcConfig: OidcConfig,
	userCustomizer: UserRepresentation.() -> CredentialRepresentation
): UserLogin {
	val user = UserRepresentation()
	user.isEnabled = true
	val password = user.userCustomizer()
	val usersRessource: UsersResource = users()
	// Create user (requires manage-users role)
	val response = usersRessource.create(user)
	val userId = CreatedResponseUtil.getCreatedId(response)

	// Define password credential
	val userResource = usersRessource[userId]
	// Set password credential
	userResource.resetPassword(password)

	val accessToken = getAccessToken(oidcConfig, user.username, password.value)

	return UserLogin(user, password.value, accessToken)
}
