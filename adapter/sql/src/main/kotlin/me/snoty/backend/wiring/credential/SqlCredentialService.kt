package me.snoty.backend.wiring.credential

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.hasAnyRole
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.credential.dto.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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
	fun ResultRow.canReadAndWrite(userId: String?, userRoles: List<Role>): Boolean {
		if (this[credentialTable.ownerId] != null && this[credentialTable.ownerId] == userId) return true

		return userRoles.hasAnyRole(Role.ADMIN, Role.MANAGE_CREDENTIALS)
	}

	/**
	 * Filter to select only credentials visible to the given user with the given roles.
	 */
	private fun useVisibleFilter(userId: String, userRoles: List<Role>): Op<Boolean> {
		val canManage = userRoles.hasAnyRole(Role.ADMIN, Role.MANAGE_CREDENTIALS)

		var accessFilter = (credentialTable.scope eq CredentialScope.GLOBAL)
			.or(credentialTable.ownerId eq userId)
			.or(credentialTable.roleRequired.inList(userRoles.map { it.name }))

		// user can manage credentials - but none belonging to other users, just global ones
		if (canManage) accessFilter = accessFilter or (credentialTable.scope eq CredentialScope.ROLE)

		return accessFilter
	}

	private fun ResultRow.dataJson(definition: CredentialDefinition): Credential {
		val serializer = definition.clazz.kotlin.serializer()
		return json.decodeFromString(serializer, this[credentialTable.data])
	}

	private fun ResultRow.dataJsonIfAccessible(definition: CredentialDefinition, userId: String, userRoles: List<Role>): Credential? {
		val accessible = canReadAndWrite(userId, userRoles)
		return if (accessible) dataJson(definition) else null
	}

	private suspend fun <T> Database.newSuspendedTransactionWithRoles(userId: String, block: suspend Transaction.(userRoles: List<Role>) -> T): T {
		val userRoles = authenticationProvider.getRolesById(userId)

		return newSuspendedTransaction {
			block(userRoles)
		}
	}

	override suspend fun create(
		userId: String,
		scope: CredentialScope,
		role: Role?,
		name: String,
		credentialType: String,
		data: Credential,
	): CredentialDto = db.newSuspendedTransactionWithRoles(userId) { _ ->
		val definition = registry.lookupByType(credentialType)

		@Suppress("UNCHECKED_CAST")
		val data = json.encodeToString(definition.clazz.kotlin.serializer() as SerializationStrategy<Credential>, data)
		val result = credentialTable.insertReturning {
			it[credentialTable.scope] = scope
			it[credentialTable.ownerId] = if (role != null) null else userId
			it[credentialTable.name] = name
			it[credentialTable.roleRequired] = role?.name
			it[credentialTable.type] = definition.type
			it[credentialTable.data] = data
		}.single()

		CredentialDto(
			scope = scope,
			id = result[credentialTable.id].value.toString(),
			name = result[credentialTable.name],
			data = result.dataJson(definition),
		)
	}

	override suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val definitions = registry.getAll()

		val count = credentialTable.id.count()
		val byType = db.newSuspendedTransaction {
			credentialTable
				.select(credentialTable.type, count)
				.groupBy(credentialTable.type)
				.where {
					(credentialTable.type inList definitions.map { it.type }) and useVisibleFilter(userId, userRoles)
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
		listCredentialsRaw(userId = userId, credentialType = credentialType) { it, _, _ ->
			EnumeratedCredentialDto(
				scope = it[credentialTable.scope],
				id = it[credentialTable.id].value.toString(),
				name = it[credentialTable.name],
			)
		}

	override suspend fun listCredentials(userId: String, credentialType: String) =
		listCredentialsRaw(userId = userId, credentialType = credentialType) { it, definition, userRoles ->
			val role = it[credentialTable.roleRequired]
				?.let { Role(it) }
			PotentiallyAccessibleCredentialDto(
				scope = it[credentialTable.scope],
				id = it[credentialTable.id].value.toString(),
				name = it[credentialTable.name],
				requiredRole = role,
				data = it.dataJsonIfAccessible(definition, userId, userRoles)
			)
		}

	private suspend fun <DTO> listCredentialsRaw(userId: String, credentialType: String, mapResultRow: (ResultRow, CredentialDefinition, List<Role>) -> DTO): Flow<DTO> {
		val definition = registry.lookupByType(credentialType)
		val userRoles = authenticationProvider.getRolesById(userId)

		return db.flowTransaction {
			credentialTable.selectAll()
				.where {
					(credentialTable.type eq definition.type) and useVisibleFilter(userId = userId, userRoles = userRoles)
				}
				.map {
					mapResultRow(it, definition, userRoles)
				}
		}
	}

	override suspend fun get(userId: String, credentialId: String): PotentiallyAccessibleCredentialDto? = db.newSuspendedTransactionWithRoles(userId) { userRoles ->
		val row = credentialTable.selectAll()
			.where {
				credentialTable.id eq credentialId.toUuid() and useVisibleFilter(userId = userId, userRoles = userRoles)
			}
			.singleOrNull()
			?: return@newSuspendedTransactionWithRoles null

		val definition = registry.lookupByType(row[credentialTable.type])
		PotentiallyAccessibleCredentialDto(
			scope = row[credentialTable.scope],
			requiredRole = row[credentialTable.roleRequired]?.let { Role(it) },
			id = row[credentialTable.id].value.toString(),
			name = row[credentialTable.name],
			data = row.dataJsonIfAccessible(definition, userId, userRoles)
		)
	}

	override suspend fun resolve(userId: String, credentialId: String): ResolvedCredential<out Credential>? = resolveImpl(userId = userId, credentialId = credentialId) {
		registry.lookupByType(this[credentialTable.type]).clazz.kotlin
	}

	override suspend fun <T : Credential> resolve(userId: String, credentialId: String, type: KClass<T>): ResolvedCredential<T>? = resolveImpl(userId = userId, credentialId = credentialId) {
		val definition = registry.lookupByType(this[credentialTable.type])

		if (definition.clazz != type.java) {
			throw IllegalArgumentException("Credential type mismatch: expected ${type.qualifiedName}, got ${definition.clazz.name}")
		}

		type
	}

	private suspend fun <T : Credential> resolveImpl(
		userId: String,
		credentialId: String,
		typeResolver: ResultRow.() -> KClass<T>,
	): ResolvedCredential<T>? = db.newSuspendedTransactionWithRoles(userId) { userRoles ->
		val row = credentialTable.selectAll()
			.where {
				credentialTable.id eq credentialId.toUuid() and useVisibleFilter(userId = userId, userRoles = userRoles)
			}
			.limit(1)
			.firstOrNull()
			?: return@newSuspendedTransactionWithRoles null
		val type = typeResolver(row)

		ResolvedCredential(
			id = row[credentialTable.id].value.toString(),
			type = row[credentialTable.type],
			data = json.decodeFromString(type.serializer(), row[credentialTable.data])
		)
	}

	override suspend fun <T : Credential> update(
		userId: String,
		credential: ResolvedCredential<T>, // access was already validated when resolving the credential
		name: String,
		data: Credential
	): CredentialDto = db.newSuspendedTransactionWithRoles(userId) { userRoles ->
		val returning = credentialTable.updateReturning(credentialTable.columns, where = {
			(credentialTable.id eq credential.id.toUuid()) and (credentialTable.type eq credential.type)
		}) {
			it[credentialTable.name] = name
			val definition = registry.lookupByType(credential.type)

			@Suppress("UNCHECKED_CAST")
			val dataString = json.encodeToString(definition.clazz.kotlin.serializer() as SerializationStrategy<Credential>, data)
			it[credentialTable.data] = dataString
		}

		val notFoundException = NotFoundException("Credential ${credential.id} not found or access denied")

		val row = returning.singleOrNull()
			?: throw notFoundException
		val definition = registry.lookupByType(row[credentialTable.type])

		val access = row.canReadAndWrite(userId, userRoles)
		if (!access) throw notFoundException

		CredentialDto(
			scope = row[credentialTable.scope],
			id = row[credentialTable.id].toString(),
			name = row[credentialTable.name],
			data = row.dataJson(definition)
		)
	}

	override suspend fun delete(credential: ResolvedCredential<*>): Boolean = db.newSuspendedTransaction {
		credentialTable.deleteWhere {
			credentialTable.id eq credential.id.toUuid()
		} == 1
	}
}
