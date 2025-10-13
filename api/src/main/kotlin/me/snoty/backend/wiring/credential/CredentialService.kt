package me.snoty.backend.wiring.credential

import me.snoty.backend.wiring.credential.dto.EnumeratedCredentialDto
import kotlin.reflect.KClass

interface CredentialService {
	suspend fun create(userId: String, name: String, credentialType: String, data: Credential): String

	suspend fun enumerateCredentials(credentialType: String, userId: String): List<EnumeratedCredentialDto>

	suspend fun resolve(credentialId: String, userId: String): Credential?
	suspend fun <T : Credential> resolve(credentialId: String, type: KClass<T>, userId: String): T?
}
