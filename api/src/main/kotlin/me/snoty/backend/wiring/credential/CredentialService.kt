package me.snoty.backend.wiring.credential

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.wiring.credential.dto.CredentialDefinitionWithStatisticsDto
import me.snoty.backend.wiring.credential.dto.CredentialDto
import me.snoty.backend.wiring.credential.dto.EnumeratedCredentialDto
import kotlin.reflect.KClass

interface CredentialService {
	suspend fun create(userId: String, name: String, credentialType: String, data: Credential): String

	suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto>

	suspend fun enumerateCredentials(userId: String, credentialType: String): Flow<EnumeratedCredentialDto>
	suspend fun listCredentials(userId: String, credentialType: String): Flow<CredentialDto>

	suspend fun resolve(userId: String, credentialId: String): Credential?
	suspend fun <T : Credential> resolve(userId: String, credentialId: String, type: KClass<T>): T?
}
