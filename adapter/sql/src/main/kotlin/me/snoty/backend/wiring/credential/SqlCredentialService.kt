package me.snoty.backend.wiring.credential

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.credential.dto.CredentialAccess
import me.snoty.backend.wiring.credential.dto.CredentialDefinitionWithStatisticsDto
import me.snoty.backend.wiring.credential.dto.CredentialDto
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
	fun ResultRow.access(userId: String) =
		if (this[credentialTable.userId] == userId) CredentialAccess.USER
		else CredentialAccess.SYSTEM

	private fun ResultRow.toCredentialDto(userId: String, definition: CredentialDefinition): CredentialDto {
		@Suppress("UNCHECKED_CAST")
		val serializer = definition.clazz.kotlin.serializer() as KSerializer<Credential>
		val data = json.decodeFromString(serializer, this[credentialTable.data])
		val dataJson = json.encodeToJsonElement(serializer, data).jsonObject

		return CredentialDto(
			id = this[credentialTable.id].value.toString(),
			name = this[credentialTable.name],
			access = this.access(userId),
			data = dataJson,
		)
	}

	override suspend fun create(userId: String, name: String, credentialType: String, data: Credential) = db.newSuspendedTransaction {
		val definition = registry.lookupByType(credentialType)
		@Suppress("UNCHECKED_CAST")
		val data = json.encodeToString(definition.clazz.kotlin.serializer() as SerializationStrategy<Credential>, data)

		val result = credentialTable.insertReturning {
			it[credentialTable.userId] = userId
			it[credentialTable.name] = name
			it[credentialTable.roleRequired] = null
			it[credentialTable.type] = definition.type
			it[credentialTable.data] = data
		}

		result.single().toCredentialDto(userId, definition)
	}

	override suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val definitions = registry.getAll()

		// TODO: replace `id.count` with `COUNT(*)`
		val count = credentialTable.id.count()
		val byType = db.newSuspendedTransaction {
			credentialTable
				.select(credentialTable.type, count)
				.groupBy(credentialTable.type)
				.where {
					(credentialTable.type inList definitions.map { it.type }) and (
						(credentialTable.userId eq userId) or (credentialTable.roleRequired inList userRoles.map(Role::name))
						)
				}
				.associate {
					it[credentialTable.type] to it[count]
				}
		}

		return definitions.map { definition ->
			CredentialDefinitionWithStatisticsDto(
				type = definition.type,
				displayName = definition.displayName,
				schema = definition.schema,
				count = byType[definition.type] ?: 0,
			)
		}.sortedByDescending { it.count }
	}

	override suspend fun enumerateCredentials(userId: String, credentialType: String) =
		listCredentialsRaw(userId = userId, credentialType = credentialType) { it, _ ->
			EnumeratedCredentialDto(
				access = it.access(userId),
				id = it[credentialTable.id].value.toString(),
				name = it[credentialTable.name],
			)
		}

	override suspend fun listCredentials(userId: String, credentialType: String) =
		listCredentialsRaw(userId = userId, credentialType = credentialType) { it, definition ->
			it.toCredentialDto(userId, definition)
		}

	private suspend fun <DTO> listCredentialsRaw(userId: String, credentialType: String, mapResultRow: (ResultRow, CredentialDefinition) -> DTO): Flow<DTO> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val definition = registry.lookupByType(credentialType)

		return db.flowTransaction {
			credentialTable.selectAll()
				.where {
					(credentialTable.type eq definition.type) and (
						(credentialTable.userId eq userId) or (credentialTable.roleRequired inList userRoles.map(Role::name))
					)
				}
				.map {
					mapResultRow(it, definition)
				}
		}
	}

	override suspend fun resolve(userId: String, credentialId: String): ResolvedCredential<out Credential>? = resolveImpl(credentialId, userId) {
		registry.lookupByType(this[credentialTable.type]).clazz.kotlin
	}

	override suspend fun <T : Credential> resolve(userId: String, credentialId: String, type: KClass<T>): ResolvedCredential<T>? = resolveImpl(userId = userId, credentialId = credentialId) {
		val definition = registry.lookupByType(this[credentialTable.type])

		if (definition.clazz != type.java) {
			throw IllegalArgumentException("Credential type mismatch: expected ${type.qualifiedName}, got ${definition.clazz.name}")
		}

		type
	}

	private suspend fun <T : Credential> resolveImpl(userId: String, credentialId: String, typeResolver: ResultRow.() -> KClass<T>): ResolvedCredential<T>? {
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

			ResolvedCredential(
				type = row[credentialTable.type],
				value = json.decodeFromString(type.serializer(), row[credentialTable.data])
			)
		}
	}
}
