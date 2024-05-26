package me.snoty.integration.common

import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.User
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.diff.EntityDiffMetrics
import me.snoty.integration.common.diff.UserEntityChanges
import me.snoty.integration.common.diff.state.UserEntityStates
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

abstract class AbstractIntegration<S : IntegrationSettings, R : JobRequest, ID : Comparable<ID>>(
	final override val name: String,
	final override val settingsType: KClass<S>,
	protected val stateCollection: MongoCollection<UserEntityStates>,
	changesCollection: MongoCollection<UserEntityChanges>,
	fetcherFactory: IntegrationFetcherFactory<R, ID>,
	meterRegistry: MeterRegistry,
	private val metricsPool: ScheduledExecutorService,
	scheduler: Scheduler,
) : Integration {
	protected val logger = KotlinLogging.logger(this::class.jvmName)

	constructor(
		name: String,
		settingsType: KClass<S>,
		fetcherFactory: IntegrationFetcherFactory<R, ID>,
		context: IntegrationContext
	) : this(
		name,
		settingsType,
		context.mongodb.getCollection<UserEntityStates>("integration.${name}.entityStates"),
		context.mongodb.getCollection<UserEntityChanges>("integration.${name}.entityChanges"),
		fetcherFactory,
		context.meterRegistry,
		context.metricsPool,
		context.scheduler
	)

	private val entityDiffMetrics: EntityDiffMetrics = EntityDiffMetrics(meterRegistry, name, stateCollection)
	override val fetcher = fetcherFactory.create(entityDiffMetrics, stateCollection, changesCollection)
	private val scheduler = IntegrationScheduler(name, scheduler)

	override fun start() {
		metricsPool.scheduleAtFixedRate(entityDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
		scheduleAll()
	}

	override fun setup(user: User, settings: IntegrationSettings)
		= IntegrationConfigTable.create(user.id, name, settings)

	private fun scheduleAll() {
		val allSettings = IntegrationConfigTable.getAllIntegrationConfigs<S>(name)
		logger.debug {
			"Scheduling ${allSettings.size} jobs for $name"
		}
		allSettings.forEach(::schedule)
	}

	abstract fun createRequest(config: IntegrationConfig<S>): JobRequest

	override fun schedule(user: User, settings: IntegrationSettings) {
		@Suppress("UNCHECKED_CAST")
		val integrationConfig = IntegrationConfig(user.id, settings as S)
		schedule(integrationConfig)
	}

	private fun schedule(config: IntegrationConfig<S>) {
		scheduler.scheduleJob(listOf(config.settings.instanceId, config.user), createRequest(config))
	}
}
