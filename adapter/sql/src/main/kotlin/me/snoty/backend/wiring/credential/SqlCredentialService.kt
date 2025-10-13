package me.snoty.backend.wiring.credential

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.credential.dto.CredentialAccess
import me.snoty.backend.wiring.credential.dto.EnumeratedCredentialDto
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

@Single
@OptIn(InternalSerializationApi::class)
class SqlCredentialService(
	private val db: Database,
	private val json: Json,
	private val registry: CredentialDefinitionRegistry,
	private val credentialTable: CredentialTable,
	private val authenticationProvider: AuthenticationProvider,
) : CredentialService {
	override suspend fun create(userId: String, name: String, credentialType: String, data: Credential) = db.newSuspendedTransaction {
		val definition = registry.lookupByType(credentialType)
		@Suppress("UNCHECKED_CAST")
		val data = json.encodeToString(definition.clazz.kotlin.serializer() as SerializationStrategy<Credential>, data)

		val result = credentialTable.insertReturning(listOf(credentialTable.id)) {
			it[credentialTable.userId] = userId
			it[credentialTable.name] = name
			it[credentialTable.roleRequired] = null
			it[credentialTable.type] = definition.type
			it[credentialTable.data] = data
		}

		result.single()[credentialTable.id].value.toString()
	}

	override suspend fun enumerateCredentials(credentialType: String, userId: String): List<EnumeratedCredentialDto> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val definition = registry.lookupByType(credentialType)

		return db.newSuspendedTransaction {
			credentialTable.selectAll()
				.where {(
					credentialTable.type eq definition.type
						and (
							(credentialTable.userId eq userId) or (credentialTable.roleRequired inList userRoles.map(Role::name))
						)
				)}
				.map {
					val access =
						if (it[credentialTable.userId] == userId) CredentialAccess.USER
					    else CredentialAccess.SYSTEM

					EnumeratedCredentialDto(
						id = it[credentialTable.id].value.toString(),
						name = it[credentialTable.name],
						access = access,
					)
				}
		}
	}

	override suspend fun resolve(credentialId: String, userId: String): Credential? = resolveImpl(credentialId, userId) {
		registry.lookupByType(this[credentialTable.type]).clazz.kotlin
	}

	override suspend fun <T : Credential> resolve(credentialId: String, type: KClass<T>, userId: String): T? = resolveImpl(credentialId, userId) {
		val definition = registry.lookupByType(this[credentialTable.type])

		if (definition.clazz != type.java) {
			throw IllegalArgumentException("Credential type mismatch: expected ${type.qualifiedName}, got ${definition.clazz.name}")
		}

		type
	}

	private suspend fun <T : Credential> resolveImpl(credentialId: String, userId: String, typeResolver: ResultRow.() -> KClass<T>): T? {
		val userRoles = authenticationProvider.getRolesById(userId)

		return db.newSuspendedTransaction {
			val row = credentialTable.selectAll()
				.where {
					(credentialTable.id eq credentialId.toUuid()) and (
						(credentialTable.userId eq userId) or (credentialTable.roleRequired inList userRoles.map(Role::name))
					)
				}
				.limit(1)
				.firstOrNull()
				?: return@newSuspendedTransaction null
			val type = typeResolver(row)

			json.decodeFromString(type.serializer(), row[credentialTable.data])
		}
	}
}
