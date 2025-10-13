package me.snoty.backend.server.resources.wiring.credential

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.snoty.backend.authentication.Role
import me.snoty.backend.wiring.credential.dto.CredentialScope

@Serializable
data class CredentialCreateDto(
	val type: String,
	val name: String,
	val scope: CredentialScope,

	/**
	 * only available when scope is [CredentialScope.ROLE]
 	 */
	val role: Role? = null,

	val data: JsonElement,
)

@Serializable
data class CredentialUpdateDto(
	val name: String,
	val data: JsonElement,
)
