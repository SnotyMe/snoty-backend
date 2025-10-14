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

	override suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val types = registry.getAll().map { it.type }

		// TODO: replace `id.count` with `COUNT(*)`
		val count = credentialTable.id.count()
		val byType = db.newSuspendedTransaction {
			credentialTable
				.select(credentialTable.type, count)
				.groupBy(credentialTable.type)
				.where {
					(credentialTable.type inList types) and (
						(credentialTable.userId eq userId) or (credentialTable.roleRequired inList userRoles.map(Role::name))
						)
				}
				.map {
					CredentialDefinitionWithStatisticsDto(
						type = it[credentialTable.type],
						count = it[count],
					)
				}
		}.associateBy { it.type }

		return types.map { type ->
			byType[type] ?: CredentialDefinitionWithStatisticsDto(type = type, count = 0)
		}.sortedByDescending { it.count }
	}

	override suspend fun enumerateCredentials(userId: String, credentialType: String) =
		listCredentialsRaw(userId = userId, credentialType = credentialType) { it, _, access ->
			EnumeratedCredentialDto(
				access = access,
				id = it[credentialTable.id].value.toString(),
				name = it[credentialTable.name],
			)
		}

	override suspend fun listCredentials(userId: String, credentialType: String) =
		listCredentialsRaw(userId = userId, credentialType = credentialType) { it, definition, access ->
			@Suppress("UNCHECKED_CAST")
			val serializer = definition.clazz.kotlin.serializer() as KSerializer<Credential>
			val data = json.decodeFromString(serializer, it[credentialTable.data])
			val dataJson = json.encodeToJsonElement(serializer, data).jsonObject

			CredentialDto(
				id = it[credentialTable.id].value.toString(),
				name = it[credentialTable.name],
				access = access,
				data = dataJson,
			)
		}

	private suspend fun <DTO> listCredentialsRaw(userId: String, credentialType: String, mapResultRow: (ResultRow, CredentialDefinition, CredentialAccess) -> DTO): Flow<DTO> {
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
					val access =
						if (it[credentialTable.userId] == userId) CredentialAccess.USER
						else CredentialAccess.SYSTEM

					mapResultRow(it, definition, access)
				}
		}
	}

	override suspend fun resolve(userId: String, credentialId: String): Credential? = resolveImpl(credentialId, userId) {
		registry.lookupByType(this[credentialTable.type]).clazz.kotlin
	}

	override suspend fun <T : Credential> resolve(userId: String, credentialId: String, type: KClass<T>): T? = resolveImpl(userId = userId, credentialId = credentialId) {
		val definition = registry.lookupByType(this[credentialTable.type])

		if (definition.clazz != type.java) {
			throw IllegalArgumentException("Credential type mismatch: expected ${type.qualifiedName}, got ${definition.clazz.name}")
		}

		type
	}

	private suspend fun <T : Credential> resolveImpl(userId: String, credentialId: String, typeResolver: ResultRow.() -> KClass<T>): T? {
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
