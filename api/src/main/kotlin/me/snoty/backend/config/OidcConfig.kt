package me.snoty.backend.config

data class OidcConfig(
	val serverUrl: String,
	val issuerUrl: String = serverUrl,
	val oidcUrl: String = "$serverUrl/protocol/openid-connect",
	val authUrl: String = "$oidcUrl/auth",
	val tokenUrl: String = "$oidcUrl/token",
	val certUrl: String = "$oidcUrl/certs",
	val userInfoUrl: String = "$oidcUrl/userinfo",
	val groupsClaim: String = "groups",
	val groupMappings: GroupMapping = mapOf(
		Group.ADMIN to "snoty-admin",
	),
	val clientId: String,
	val clientSecret: String,
)

/**
 * Key: Snoty Group
 * Value: External Group
 */
typealias GroupMapping = Map<String, String>

/**
 * @return the external Group for the given Snoty Group
 */
fun GroupMapping.getMapping(snotyGroup: String) = get(snotyGroup) ?: snotyGroup

/**
 * @return the Snoty Group for the given external Group
 */
fun GroupMapping.getReverseMapping(externalGroup: String) =
	entries.firstOrNull { (_, external) -> externalGroup == external }?.key

object Group {
	const val ADMIN = "admin"
}
