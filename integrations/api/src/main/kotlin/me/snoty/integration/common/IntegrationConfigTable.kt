package me.snoty.integration.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class IntegrationConfig<S>(val user: UUID, val settings: S)

val mapper = jacksonObjectMapper()

object IntegrationConfigTable : LongIdTable() {
	val user = uuid("user")
	private val integrationType = varchar("integration_type", 255)
	private val settings = jsonb<IntegrationSettings>(
		"config",
		{ mapper.writeValueAsString(it) },
		{ mapper.readValue(it) }
	)

	init {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(this@IntegrationConfigTable)
		}
	}

	fun <S> getAllIntegrationConfigs(integrationType: String) = transaction {
		select(settings, user)
			.where { IntegrationConfigTable.integrationType eq integrationType }
			.map { row ->
				@Suppress("UNCHECKED_CAST")
				IntegrationConfig(row[user], row[settings] as S)
			}
	}

	fun <S> get(id: Long, integrationType: String) = transaction {
		@Suppress("UNCHECKED_CAST")
		select(settings)
			.where { IntegrationConfigTable.id eq id and (IntegrationConfigTable.integrationType eq integrationType) }
			.firstOrNull()
			?.get(settings) as S?
	}

	fun <S : IntegrationSettings> create(userId: UUID, integrationType: String, settings: S) = transaction {
		insertAndGetId {
			it[user] = userId
			it[this.integrationType] = integrationType
			it[this.settings] = settings
		}.value
	}
}
