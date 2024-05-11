package me.snoty.backend.integration.common

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.User
import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.integration.common.diff.EntityStateTable
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.Scheduler
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

abstract class AbstractIntegration<S : IntegrationSettings, R : JobRequest>(
	final override val name: String,
	final override val settingsType: KClass<S>,
	private val stateTable: EntityStateTable<*>,
	fetcherFactory: IntegrationFetcherFactory<R>,
	database: Database,
	meterRegistry: MeterRegistry,
	private val metricsPool: ScheduledExecutorService,
	scheduler: Scheduler
) : Integration {
	constructor(
		name: String,
		settingsType: KClass<S>,
		stateTable: EntityStateTable<*>,
		fetcherFactory: IntegrationFetcherFactory<R>,
		context: IntegrationContext
	) : this(
		name,
		settingsType,
		stateTable,
		fetcherFactory,
		context.database,
		context.meterRegistry,
		context.metricsPool,
		context.scheduler
	)

	private val entityDiffMetrics: EntityDiffMetrics = EntityDiffMetrics(meterRegistry, database, name, stateTable)
	override val fetcher = fetcherFactory.create(entityDiffMetrics)
	private val scheduler = IntegrationScheduler(name, scheduler)

	override fun start() {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(stateTable)
		}
		metricsPool.scheduleAtFixedRate(entityDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
		scheduleAll()
	}

	private fun scheduleAll() {
		val allSettings = IntegrationConfigTable.getAllIntegrationConfigs<S>(name)
		allSettings.forEach(::schedule)
	}

	abstract fun createRequest(config: IntegrationConfig<S>): JobRequest
	abstract fun getInstanceId(config: IntegrationConfig<S>): Int

	override fun schedule(user: User, settings: Any) {
		@Suppress("UNCHECKED_CAST")
		val integrationConfig = IntegrationConfig(user.id, settings as S)
		schedule(integrationConfig)
	}

	private fun schedule(config: IntegrationConfig<S>) {
		scheduler.scheduleJob(listOf(getInstanceId(config), config.user), createRequest(config))
	}
}
