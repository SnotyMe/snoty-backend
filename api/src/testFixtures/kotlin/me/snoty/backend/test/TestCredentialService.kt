package me.snoty.backend.test

import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialRef
import me.snoty.backend.wiring.credential.CredentialService
import me.snoty.backend.wiring.credential.dto.EnumeratedCredentialDto
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@Suppress("UNCHECKED_CAST")
object TestCredentialService : CredentialService {
	private val credentials = mutableMapOf<Uuid, Credential>()

	override suspend fun <T : Credential> getAvailableCredentials(
		credentialType: String,
		userId: String
	): List<EnumeratedCredentialDto> = throw NotImplementedError()

	override suspend fun <T : Credential> resolve(
		ref: CredentialRef<T>,
		type: KClass<T>,
		userId: String
	) = credentials[ref.credentialId] as? T

	fun <T : Credential> create(data: T): CredentialRef<T> {
		val id = Uuid.random()
		credentials[id] = data
		return CredentialRef(id)
	}
}
