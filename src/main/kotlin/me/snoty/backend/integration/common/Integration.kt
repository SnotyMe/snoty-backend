package me.snoty.backend.integration.common

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.User
import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.integration.common.diff.EntityStateTable
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

interface Integration {
	val name: String
	val settingsType: KClass<out Any>
	val scheduler: IntegrationScheduler<Any>

	fun start()
	fun schedule(user: User, settings: Any)
}

abstract class AbstractIntegration<S : Any>(
	override val name: String,
	override val settingsType: KClass<S>,
	stateTable: EntityStateTable<*>,
	schedulerFactory: IntegrationSchedulerFactory<S>,
	database: Database,
	meterRegistry: MeterRegistry,
	private val metricsPool: ScheduledExecutorService
) : Integration {
	constructor(
		name: String,
		settingsType: KClass<S>,
		stateTable: EntityStateTable<*>,
		schedulerFactory: IntegrationSchedulerFactory<S>,
		context: IntegrationContext
	) : this(
		name,
		settingsType,
		stateTable,
		schedulerFactory,
		context.database,
		context.meterRegistry,
		context.metricsPool
	)

	private val entityDiffMetrics: EntityDiffMetrics
		= EntityDiffMetrics(meterRegistry, database, name, stateTable)
	@Suppress("UNCHECKED_CAST")
	override val scheduler: IntegrationScheduler<Any>
		= schedulerFactory.create(entityDiffMetrics) as IntegrationScheduler<Any>

	override fun start() {
		metricsPool.scheduleAtFixedRate(entityDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
		scheduleAll()
	}

	private fun scheduleAll() {
		val allSettings = IntegrationConfigTable.getAllIntegrationConfigs<S>(name)
		allSettings.forEach {
			@Suppress("UNCHECKED_CAST")
			scheduler.schedule(it as IntegrationConfig<Any>)
		}
	}

	override fun schedule(user: User, settings: Any) {
		@Suppress("UNCHECKED_CAST")
		scheduler.schedule(IntegrationConfig(user.id, settings as S))
	}
}
