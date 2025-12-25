package me.snoty.backend.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.snoty.backend.authentication.Role
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialService
import me.snoty.backend.wiring.credential.ResolvedCredential
import me.snoty.backend.wiring.credential.dto.*
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@Suppress("UNCHECKED_CAST")
object TestCredentialService : CredentialService {
	data class CredentialValue(
		val userId: String,
		val name: String,
		val type: String,
		val data: Credential,
	)

	private val credentials = mutableMapOf<Uuid, CredentialValue>()

	override suspend fun create(userId: String, scope: CredentialScope, role: Role?, name: String, credentialType: String, data: Credential): CredentialDto {
		val id = Uuid.random()
		credentials[id] = CredentialValue(
			userId = userId,
			name = name,
			type = credentialType,
			data = data,
		)
		return CredentialDto(
			id = id.toString(),
			name = name,
			scope = CredentialScope.USER,
			data = data,
		)
	}

	override suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto> = throw NotImplementedError()

	override suspend fun enumerateCredentials(
		userId: String,
		credentialType: String
	): Flow<EnumeratedCredentialDto> = credentials
		.filterValues { it.userId == userId && it.type == credentialType }
		.map { (id, value) -> EnumeratedCredentialDto(CredentialScope.USER, id = id.toString(), name = value.name) }
		.asFlow()

	override suspend fun listCredentials(userId: String, credentialType: String): Flow<PotentiallyAccessibleCredentialDto> = credentials
		.filterValues { it.userId == userId && it.type == credentialType }
		.map { (id, value) ->
			PotentiallyAccessibleCredentialDto(
				id = id.toString(),
				name = value.name,
				scope = CredentialScope.USER,
				requiredRole = null,
				data = value.data,
			)
		}
		.asFlow()

	override suspend fun resolve(
		userId: String,
		credentialId: String
	): ResolvedCredential<out Credential>? = credentials[credentialId.toUuid()].let {
		if (it == null || it.userId != userId) {
			return null
		}
		ResolvedCredential(
			id = credentialId,
			type = it.type,
			data = it.data,
		)
	}

	override suspend fun get(userId: String, credentialId: String): PotentiallyAccessibleCredentialDto = throw NotImplementedError()

	override suspend fun <T : Credential> resolve(
		userId: String,
		credentialId: String,
		type: KClass<T>
	): ResolvedCredential<T> = throw NotImplementedError()

	override suspend fun <T : Credential> update(userId: String, credential: ResolvedCredential<T>, name: String, data: Credential): CredentialDto {
		val id = credential.id.toUuid()
		val existing = credentials[id] ?: throw IllegalArgumentException("Credential not found")
		if (existing.userId != userId) {
			throw IllegalArgumentException("Credential not found")
		}
		credentials[id] = CredentialValue(
			userId = userId,
			name = name,
			type = existing.type,
			data = data,
		)
		return CredentialDto(
			id = credential.id,
			name = name,
			scope = CredentialScope.USER,
			data = data,
		)
	}

	override suspend fun delete(credential: ResolvedCredential<*>): Boolean {
		val id = credential.id.toUuid()
		credentials.remove(id)
		return true
	}
}
