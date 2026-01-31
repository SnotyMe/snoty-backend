package me.snoty.backend.wiring.credential

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.model.Projections.projection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.database.mongo.objectId
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.bson.decode
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.wiring.credential.dto.*
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

@Single
class MongoCredentialService(
	db: MongoDatabase,
	private val registry: CredentialDefinitionRegistry,
	private val authenticationProvider: AuthenticationProvider,
	private val codecRegistry: CodecRegistry
) : CredentialService {
	private val collection = db.getCollection<MongoCredential>("credentials")

	override suspend fun create(
		userId: String,
		scope: CredentialScope,
		role: Role?,
		name: String,
		credentialType: String,
		data: Credential
	): CredentialDto {
		val definition = registry.lookupByType(credentialType)

		val data = codecRegistry.encode(data)
		val credential = MongoCredential(
			scope = scope,
			ownerId = userId,
			roleRequired = role?.name,
			type = definition.type,
			name = name,
			data = data
		)

		collection.insertOne(credential)

		return credential.toDto(codecRegistry, definition)
	}

	override suspend fun listDefinitionsWithStatistics(userId: String): List<CredentialDefinitionWithStatisticsDto> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val definitions = registry.getAll()
		val types = definitions.map { it.type }

		val matchFilter = Filters.and(
			Filters.`in`(MongoCredential::type.name, types),
			CredentialFilters.credentialVisible(userId, userRoles)
		)
		val count = "count"
		val pipeline = listOf(
			Aggregates.match(matchFilter),
			Aggregates.group(
				MongoCredential::type.projection,
			Accumulators.sum(count, 1L)
			)
		)

		val byType= collection.aggregate<Document>(pipeline)
			.toList()
			.associate { doc ->
				val id = doc.getString(MongoCredential::_id.name)
				val count = doc.getLong(count)
				id to count
			}

		return definitions.map { definition ->
			CredentialDefinitionWithStatisticsDto(
				type = definition.type,
				displayName = definition.displayName,
				schema = definition.schema,
				count = byType[definition.type] ?: 0
			)
		}.sortedByDescending { it.count }
	}

	override suspend fun enumerateCredentials(
		userId: String,
		credentialType: String
	): Flow<EnumeratedCredentialDto> {
		val roles = authenticationProvider.getRolesById(userId)

		return collection.find(
			Filters.and(
				Filters.eq(MongoCredential::type.name, credentialType),
				CredentialFilters.credentialVisible(userId, roles)
			)
		).map {
			EnumeratedCredentialDto(
				id = it._id.toHexString(),
				scope = it.scope,
				name = it.name,
			)
		}
	}

	override suspend fun listCredentials(
		userId: String,
		credentialType: String
	): Flow<PotentiallyAccessibleCredentialDto> {
		val userRoles = authenticationProvider.getRolesById(userId)
		val definition = registry.lookupByType(credentialType)

		return collection.find(
			Filters.and(
				Filters.eq(MongoCredential::type.name, credentialType),
				CredentialFilters.credentialVisible(userId, userRoles)
			)
		).map { credential ->
			val accessible = credential.canReadAndWrite(userId, userRoles)
			credential.toPotentiallyAccessibleDto(
				codecRegistry = codecRegistry,
				definition = definition,
				accessible = accessible,
			)
		}
	}

	override suspend fun get(userId: String, credentialId: String): PotentiallyAccessibleCredentialDto? {
		val userRoles = authenticationProvider.getRolesById(userId)
		val credential = collection
			.find(Filters.and(
				Filters.eq(MongoCredential::_id.name, credentialId.objectId),
				CredentialFilters.credentialVisible(userId, userRoles)
			))
			.firstOrNull()
			?: return null

		val definition = registry.lookupByType(credential.type)
		val accessible = credential.canReadAndWrite(userId, userRoles)
		return credential.toPotentiallyAccessibleDto(
			codecRegistry = codecRegistry,
			definition = definition,
			accessible = accessible,
		)
	}

	override suspend fun resolve(
		userId: String,
		credentialId: String
	): ResolvedCredential<out Credential>? = resolveImpl(userId = userId, credentialId = credentialId) {
		registry.lookupByType(it.type).clazz.kotlin
	}

	override suspend fun <T : Credential> resolve(
		userId: String,
		credentialId: String,
		type: KClass<T>
	): ResolvedCredential<T>? = resolveImpl(userId, credentialId) {
		val definition = registry.lookupByType(it.type)

		if (definition.clazz != type.java) {
			throw IllegalArgumentException("Credential type mismatch: expected ${type.qualifiedName}, got ${definition.clazz.name}")
		}

		type
	}

	private suspend fun <T : Credential> resolveImpl(
		userId: String,
		credentialId: String,
		typeResolver: (credential: MongoCredential) -> KClass<T>,
	): ResolvedCredential<T>? {
		val userRoles = authenticationProvider.getRolesById(userId)
		val credential = collection.find(
			Filters.and(
				Filters.eq(MongoCredential::_id.name, credentialId.objectId),
				CredentialFilters.credentialVisible(userId, userRoles)
			)
		).firstOrNull() ?: return null

		val type = typeResolver(credential)
		return ResolvedCredential(
			id = credential._id.toHexString(),
			type = credential.type,
			data = codecRegistry.decode(type, credential.data)
		)
	}

	override suspend fun <T : Credential> update(
		userId: String,
		credential: ResolvedCredential<T>,
		name: String,
		data: Credential
	): CredentialDto {
		val userRoles = authenticationProvider.getRolesById(userId)

		val notFoundException = NotFoundException("Credential ${credential.id} not found or access denied")

		val old = collection.find(
			Filters.eq(MongoCredential::_id.name, credential.id.objectId)
		).firstOrNull() ?: throw notFoundException

		if (!old.canReadAndWrite(userId, userRoles)) throw notFoundException

		val dataDocument = codecRegistry.encode(data)

		collection.updateOne(
			Filters.eq(MongoCredential::_id.name, credential.id.objectId),
			Updates.combine(
				Updates.set(MongoCredential::name.name, name),
				Updates.set(MongoCredential::data.name, dataDocument)
			),
		)

		val new = collection.find(
			Filters.eq(MongoCredential::_id.name, credential.id.objectId)
		).firstOrNull() ?: throw notFoundException

		val definition = registry.lookupByType(old.type)
		return new.toDto(codecRegistry, definition)
	}

	override suspend fun delete(credential: ResolvedCredential<*>): Boolean {
		return collection.deleteOne(
			Filters.eq(MongoCredential::_id.name, credential.id.objectId)
		).deletedCount == 1L
	}
}
