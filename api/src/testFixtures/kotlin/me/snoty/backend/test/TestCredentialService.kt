package me.snoty.backend.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.snoty.backend.authentication.Role
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialService
import me.snoty.backend.wiring.credential.ResolvedCredential
import me.snoty.backend.wiring.credential.dto.*
import me.snoty.core.user.UserId
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@Suppress("UNCHECKED_CAST")
object TestCredentialService : CredentialService {
	data class CredentialValue(
		val userId: UserId,
		val type: String,
		val name: String,
		val data: Credential,
	)

	private val credentials = mutableMapOf<Uuid, CredentialValue>()

	override suspend fun create(userId: UserId, scope: CredentialScope, role: Role?, name: String, credentialType: String, data: Credential): CredentialDto {
		val id = Uuid.random()
		credentials[id] = CredentialValue(
			userId = userId,
			type = credentialType,
			name = name,
			data = data,
		)
		return CredentialDto(
			id = id.toString(),
			type = credentialType,
			scope = CredentialScope.USER,
			name = name,
			data = data,
		)
	}

	override suspend fun listDefinitionsWithStatistics(userId: UserId): List<CredentialDefinitionWithStatisticsDto> = throw NotImplementedError()

	override suspend fun enumerateCredentials(
		userId: UserId,
		credentialType: String
	): Flow<EnumeratedCredentialDto> = credentials
		.filterValues { it.userId == userId && it.type == credentialType }
		.map { (id, value) -> EnumeratedCredentialDto(CredentialScope.USER, id = id.toString(), name = value.name) }
		.asFlow()

	override suspend fun listCredentials(userId: UserId, credentialType: String?): Flow<PotentiallyAccessibleCredentialDto> = credentials
		.filterValues { it.userId == userId && (credentialType == null || it.type == credentialType) }
		.map { (id, value) ->
			PotentiallyAccessibleCredentialDto(
				id = id.toString(),
				type = value.type,
				scope = CredentialScope.USER,
				name = value.name,
				requiredRole = null,
				data = value.data,
			)
		}
		.asFlow()

	override suspend fun resolve(
		userId: UserId,
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

	override suspend fun get(userId: UserId, credentialId: String): PotentiallyAccessibleCredentialDto = throw NotImplementedError()

	override suspend fun <T : Credential> resolve(
		userId: UserId,
		credentialId: String,
		type: KClass<T>
	): ResolvedCredential<T> = throw NotImplementedError()

	override suspend fun <T : Credential> update(userId: UserId, credential: ResolvedCredential<T>, name: String, data: Credential): CredentialDto {
		val id = credential.id.toUuid()
		val existing = credentials[id] ?: throw IllegalArgumentException("Credential not found")
		if (existing.userId != userId) {
			throw IllegalArgumentException("Credential not found")
		}
		credentials[id] = CredentialValue(
			userId = userId,
			type = existing.type,
			name = name,
			data = data,
		)
		return CredentialDto(
			id = credential.id,
			scope = CredentialScope.USER,
			type = existing.type,
			name = name,
			data = data,
		)
	}

	override suspend fun delete(credential: ResolvedCredential<*>): Boolean {
		val id = credential.id.toUuid()
		credentials.remove(id)
		return true
	}
}
