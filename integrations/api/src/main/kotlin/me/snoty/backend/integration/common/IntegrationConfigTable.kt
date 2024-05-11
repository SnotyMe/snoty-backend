package me.snoty.backend.integration.common

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class IntegrationConfig<S>(val user: UUID, val settings: S)

object IntegrationConfigTable : IdTable<Long>() {
	override val id = long("id").entityId()

	val user = uuid("user")
	private val integrationType = varchar("integration_type", 255)
	private val settings = jsonb<IntegrationSettings>("config", Json)

	init {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(this@IntegrationConfigTable)
		}
	}

	fun <S> getAllIntegrationConfigs(integrationType: String) = transaction {
		select(settings, user)
			.where { this@IntegrationConfigTable.integrationType eq integrationType }
			.map { row ->
				@Suppress("UNCHECKED_CAST")
				IntegrationConfig(row[user], row[settings] as S)
			}
	}
}
