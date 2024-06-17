package me.snoty.backend.integration.config

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.*
import me.snoty.integration.common.IntegrationConfig
import me.snoty.integration.common.IntegrationSettings
import me.snoty.integration.common.config.IntegrationConfigService
import org.bson.Document
import java.util.*
import kotlin.reflect.KClass

class MongoIntegrationConfigService(db: MongoDatabase) : IntegrationConfigService {
	private val collection = db.getCollection<UserIntegrationConfig>("integration_config")

	override fun <S : IntegrationSettings> getAll(integrationType: String, clazz: KClass<S>): Flow<IntegrationConfig<S>> {
		// the UntypedIntegrationConfig is needed because the generics of generics are erased
		// as the kotlin driver just eventually uses java's `Class` in the background
		// which, due to type erasure, doesn't store the generic type
		data class UntypedIntegrationConfig(val user: UUID, val settings: Document)

		return collection.aggregate<UntypedIntegrationConfig>(
			Aggregates.unwind("\$${UserIntegrationConfig::sources.name}.$integrationType"),
			Aggregations.project(
				Projections.exclude("_id"),
				Projections.computed(UntypedIntegrationConfig::user.name, "\$_id"),
				Projections.computed(
					UntypedIntegrationConfig::settings.name,
					"\$${UserIntegrationConfig::sources.name}.$integrationType"
				)
			)
		).map {
			val settings = collection.codecRegistry.decode(clazz, it.settings)
			IntegrationConfig(it.user, settings)
		}
	}

	override suspend fun <S : IntegrationSettings> get(id: ConfigId, integrationType: String, clazz: KClass<S>): S? {
		val settings = collection.getByIdFromArray<Document>("${UserIntegrationConfig::sources.name}.$integrationType", id) ?: return null
		return collection.codecRegistry.decode(clazz, settings)
	}

	override suspend fun <S : IntegrationSettings> create(userID: UUID, integrationType: String, settings: S): ConfigId {
		val filter = Filters.eq("_id", userID)
		val update = Updates.push("${UserIntegrationConfig::sources.name}.$integrationType", settings)
		collection.upsertOne(filter, update)
		return settings.id
	}
}
