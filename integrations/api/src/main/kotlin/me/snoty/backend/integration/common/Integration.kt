package me.snoty.backend.integration.common

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.User
import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.integration.common.diff.EntityStateTable
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.Scheduler
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

interface Integration {
	val name: String
	val settingsType: KClass<out Any>
	val fetcher: Fetcher<*>

	fun start()
	fun schedule(user: User, settings: Any)
}

interface IntegrationFactory {
	fun create(context: IntegrationContext): Integration
}

abstract class AbstractIntegration<S : Any, R : JobRequest>(
	override val name: String,
	override val settingsType: KClass<S>,
	stateTable: EntityStateTable<*>,
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
		schedulerFactory: IntegrationFetcherFactory<R>,
		context: IntegrationContext
	) : this(
		name,
		settingsType,
		stateTable,
		schedulerFactory,
		context.database,
		context.meterRegistry,
		context.metricsPool,
		context.scheduler
	)

	private val entityDiffMetrics: EntityDiffMetrics = EntityDiffMetrics(meterRegistry, database, name, stateTable)

	override val fetcher = fetcherFactory.create(entityDiffMetrics)
	private val scheduler = SchedulerForIntegrations(name, scheduler)

	override fun start() {
		metricsPool.scheduleAtFixedRate(entityDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
		scheduleAll()
	}

	private fun scheduleAll() {
		val allSettings = IntegrationConfigTable.getAllIntegrationConfigs<S>(name)
		allSettings.forEach { config ->
			schedule(config)
		}
	}

	abstract fun createRequest(config: IntegrationConfig<S>): org.jobrunr.jobs.lambdas.JobRequest
	abstract fun getInstanceId(config: IntegrationConfig<S>): Any

	override fun schedule(user: User, settings: Any) {
		val integrationConfig = IntegrationConfig(user.id, settings as S)
		schedule(integrationConfig)
	}

	private fun schedule(config: IntegrationConfig<S>) {
		scheduler.scheduleJob(listOf(getInstanceId(config), config.user), createRequest(config))
	}
}
