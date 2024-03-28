package me.snoty.backend.dev.auth

import me.snoty.backend.dev.randomString
import org.keycloak.admin.client.resource.RealmsResource
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.days

class KeycloakConfigurer(private var realmsResource: RealmsResource, private var realmName: String) {
	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	/**
	 * Configures the realm, first validates if the realm exists and if none exists, creates the realm.
	 */
	fun configure(): KeycloakConfigurationResult {
		val realms = realmsResource.findAll()
		if (realms.stream().noneMatch { realm: RealmRepresentation -> realm.id == realmName }) {
			logger.info("Realm {} does not exist yet, creating...", realmName)
			createRealm(realmName, realmsResource)
		}
		return updateRealm()
	}

	private fun createRealm(realmName: String?, realmsResource: RealmsResource?) {
		val realmRepresentation = RealmRepresentation()
		realmRepresentation.displayName = realmName
		realmRepresentation.id = realmName
		realmRepresentation.realm = realmName
		realmRepresentation.isEnabled = false

		realmsResource!!.create(realmRepresentation)
		logger.info("Created realm '{}'", realmName)
	}

	private fun updateRealm(): KeycloakConfigurationResult {
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
			logger.info("Client 'snoty' does not exist yet, creating...")
				ClientRepresentation().apply {
					clientId = "snoty"
					secret = randomString(64)
					isDirectAccessGrantsEnabled = true
					isServiceAccountsEnabled = true
					redirectUris = listOf("http://localhost:8080/*")
					clientsResource.create(this)
					logger.info("Created client 'snoty'")
				}
			})
		.let {
			return@let KeycloakConfigurationResult(it.clientId, it.secret)
		}
	}
}
