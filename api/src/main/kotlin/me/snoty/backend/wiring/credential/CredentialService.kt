package me.snoty.backend.wiring.credential

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.authentication.Role
import me.snoty.backend.wiring.credential.dto.*
import me.snoty.core.UserId
import kotlin.reflect.KClass

interface CredentialService {
	suspend fun create(userId: UserId, scope: CredentialScope, role: Role?, name: String, credentialType: String, data: Credential): CredentialDto

	suspend fun listDefinitionsWithStatistics(userId: UserId): List<CredentialDefinitionWithStatisticsDto>

	suspend fun enumerateCredentials(userId: UserId, credentialType: String): Flow<EnumeratedCredentialDto>
	suspend fun listCredentials(userId: UserId, credentialType: String): Flow<PotentiallyAccessibleCredentialDto>

	suspend fun get(userId: UserId, credentialId: String): PotentiallyAccessibleCredentialDto?
	suspend fun resolve(userId: UserId, credentialId: String): ResolvedCredential<out Credential>?
	suspend fun <T : Credential> resolve(userId: UserId, credentialId: String, type: KClass<T>): ResolvedCredential<T>?

	/**
	 * Modifies the credential with new data WITHOUT checking ownership or access rights.
	 */
	suspend fun <T : Credential> update(
		userId: UserId,
		credential: ResolvedCredential<T>,
		name: String,
		data: Credential,
	): CredentialDto

	suspend fun delete(credential: ResolvedCredential<*>): Boolean
}
