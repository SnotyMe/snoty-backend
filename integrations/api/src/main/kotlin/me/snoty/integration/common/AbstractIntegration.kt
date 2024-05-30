package me.snoty.integration.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import me.snoty.backend.User
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.config.IntegrationConfigService
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
	protected val integrationConfigService: IntegrationConfigService,
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
		context.integrationConfigService,
		context.scheduler
	)

	override val fetcher = fetcherFactory.create(entityStateService)
	private val scheduler = IntegrationScheduler(name, scheduler)

	override suspend fun start() {
		entityStateService.scheduleMetricsTask()
		scheduleAll()
	}

	override suspend fun setup(user: User, settings: IntegrationSettings)
		= integrationConfigService.create(user.id, name, settings)

	private suspend fun scheduleAll() {
		val allSettings = integrationConfigService.getAll(name, settingsType)
		val count = allSettings.count()
		logger.debug {
			"Scheduling $count jobs for $name"
		}
		allSettings.collect {
			schedule(it)
		}
	}

	abstract fun createRequest(config: IntegrationConfig<S>): JobRequest

	override suspend fun schedule(user: User, settings: IntegrationSettings) {
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
