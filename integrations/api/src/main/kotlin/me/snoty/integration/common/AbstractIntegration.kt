package me.snoty.integration.common

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.User
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.fetch.IntegrationFetcherFactory
import me.snoty.integration.common.utils.createFetcherJob
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

abstract class AbstractIntegration<S : IntegrationSettings, R : JobRequest, ID : Comparable<ID>>(
	final override val descriptor: IntegrationDescriptor,
	final override val settingsType: KClass<S>,
	fetcherFactory: IntegrationFetcherFactory<R, ID>,
	protected val entityStateService: EntityStateService,
	scheduler: Scheduler,
) : Integration {
	protected val logger = KotlinLogging.logger(this::class.jvmName)

	constructor(
		descriptor: IntegrationDescriptor,
		settingsType: KClass<S>,
		fetcherFactory: IntegrationFetcherFactory<R, ID>,
		context: IntegrationContext
	) : this(
		descriptor,
		settingsType,
		fetcherFactory,
		context.entityStateService,
		context.scheduler
	)

	override val fetcher = fetcherFactory.create(entityStateService)
	private val scheduler = IntegrationScheduler(name, scheduler)

	override fun start() {
		entityStateService.scheduleMetricsTask()
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
		val idParts = listOf(config.settings.instanceId, config.user)
		val request = createRequest(config)
		val job = createFetcherJob(descriptor, config, request)
		scheduler.scheduleJob(idParts, job)
	}
}
