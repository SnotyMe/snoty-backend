package me.snoty.backend.dev.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.ws.rs.core.Response
import me.snoty.backend.dev.randomString
import org.keycloak.admin.client.resource.RealmsResource
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.ProtocolMapperRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import kotlin.time.Duration.Companion.days

class KeycloakConfigurer(private var realmsResource: RealmsResource, private var realmName: String) {
	private val logger = KotlinLogging.logger {}

	/**
	 * Configures the realm, first validates if the realm exists and if none exists, creates the realm.
	 */
	fun configure(vararg extraRedirectUris: String): KeycloakConfigurationResult {
		val realms = realmsResource.findAll()
		if (realms.stream().noneMatch { realm: RealmRepresentation -> realm.id == realmName }) {
			logger.info { "Realm $realmName does not exist yet, creating..." }
			createRealm(realmName, realmsResource)
		}
		return updateRealm(*extraRedirectUris)
	}

	private fun createRealm(realmName: String?, realmsResource: RealmsResource?) {
		val realmRepresentation = RealmRepresentation()
		realmRepresentation.displayName = realmName
		realmRepresentation.id = realmName
		realmRepresentation.realm = realmName
		realmRepresentation.isEnabled = false

		realmsResource!!.create(realmRepresentation)
		logger.info { "Created realm '$realmName'" }
	}

	private fun updateRealm(vararg extraRedirectUris: String): KeycloakConfigurationResult {
		val realmRepresentation = RealmRepresentation().apply {
			isBruteForceProtected = true
			isEnabled = true
			isRegistrationAllowed = true
			isEditUsernameAllowed = true
			// avoids browser-side https enforcement
			browserSecurityHeaders = mapOf("strictTransportSecurity" to "")
			ssoSessionIdleTimeout = 7.days.inWholeSeconds.toInt()
			ssoSessionMaxLifespan = 30.days.inWholeSeconds.toInt()
			accessTokenLifespan = 1.days.inWholeSeconds.toInt()
		}
		val realmResource = realmsResource.realm(realmName)
		realmResource.update(realmRepresentation)

		val clientsResource = realmResource.clients()
		return (clientsResource.findByClientId("snoty").firstOrNull() ?: let {
			logger.info { "Client 'snoty' does not exist yet, creating..." }
				ClientRepresentation().apply {
					clientId = "snoty"
					secret = randomString(64)
					isDirectAccessGrantsEnabled = true
					isServiceAccountsEnabled = true
					redirectUris = listOf("http://localhost:8080/*", "http://localhost:5173/*", *extraRedirectUris)
						protocolMappers = listOf(ProtocolMapperRepresentation().apply {
						name = "Group Mapper"
						protocol = "openid-connect"
						protocolMapper = "oidc-group-membership-mapper"
						// https://github.com/keycloak/keycloak/blob/main/services/src/main/java/org/keycloak/protocol/oidc/mappers/OIDCAttributeMapperHelper.java#L57
						config = mapOf(
							"full-path" to false.toString(),
							"claim.name" to "groups",
							"id.token.claim" to true.toString(),
							"access.token.claim" to true.toString(),
							"userinfo.token.claim" to true.toString()
						)
					})
					val response = clientsResource.create(this)
					if (response.statusInfo.family != Response.Status.Family.SUCCESSFUL) {
						logger.error { "Error creating client: HTTP ${response.status} - ${response.readEntity(String::class.java)}" }
					} else {
						logger.info { "Created client 'snoty'" }
					}
				}
			})
		.let {
			return@let KeycloakConfigurationResult(it.clientId, it.secret)
		}
	}
}
