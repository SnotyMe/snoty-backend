package me.snoty.backend.wiring.credential

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.authentication.Role
import me.snoty.backend.wiring.credential.dto.*
import kotlin.reflect.KClass

interface CredentialService {
	suspend fun create(userId: String, scope: CredentialScope, role: Role?, name: String, credentialType: String, data: Credential): CredentialDto

	suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto>

	suspend fun enumerateCredentials(userId: String, credentialType: String): Flow<EnumeratedCredentialDto>
	suspend fun listCredentials(userId: String, credentialType: String): Flow<PotentiallyAccessibleCredentialDto>

	suspend fun get(userId: String, credentialId: String): PotentiallyAccessibleCredentialDto?
	suspend fun resolve(userId: String, credentialId: String): ResolvedCredential<out Credential>?
	suspend fun <T : Credential> resolve(userId: String, credentialId: String, type: KClass<T>): ResolvedCredential<T>?

	/**
	 * Modifies the credential with new data WITHOUT checking ownership or access rights.
	 */
	suspend fun <T : Credential> update(
		userId: String,
		credential: ResolvedCredential<T>,
		name: String,
		data: Credential,
	): CredentialDto

	suspend fun delete(credential: ResolvedCredential<*>): Boolean
}
