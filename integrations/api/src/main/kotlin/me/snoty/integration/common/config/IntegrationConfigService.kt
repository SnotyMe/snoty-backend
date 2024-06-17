package me.snoty.integration.common.config

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.ConfigId
import me.snoty.integration.common.IntegrationConfig
import me.snoty.integration.common.IntegrationSettings
import java.util.*
import kotlin.reflect.KClass

interface IntegrationConfigService {
	fun <S : IntegrationSettings> getAll(integrationType: String, clazz: KClass<S>): Flow<IntegrationConfig<S>>

	suspend fun <S : IntegrationSettings> get(id: ConfigId, integrationType: String, clazz: KClass<S>): S?

	/**
	 * @return ID of the newly created entry
	 */
	suspend fun <S : IntegrationSettings> create(userID: UUID, integrationType: String, settings: S): ConfigId
}

suspend inline fun <reified S : IntegrationSettings> IntegrationConfigService.get(id: ConfigId, integrationType: String)
	= get(id, integrationType, S::class)
