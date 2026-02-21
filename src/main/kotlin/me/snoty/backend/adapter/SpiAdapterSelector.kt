package me.snoty.backend.adapter

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import java.util.*
import kotlin.reflect.KClass

data class AdapterConfig(val adapter: String)

@Single
class SpiAdapterSelector(
	private val configLoader: ConfigLoader,
	private val koin: Koin,
) : AdapterSelector {
	private val logger = KotlinLogging.logger {}

	override fun <T : Adapter> load(adapterClass: KClass<T>, configKey: String): T {
		val providers = ServiceLoader.load(adapterClass.java).toList()

		val adapterConfig: AdapterConfig = configLoader.load(configKey) {
			providers.forEach { it.autoconfigure(this) }
		}

		val potential = providers.filter {
			adapterConfig.adapter in it.supportedTypes
		}
		logger.info { "Adapter selection for $configKey: requested=${adapterConfig.adapter} potential=${potential.map { it::class.simpleName }}" }

		val selected = potential.singleOrNull()
		when {
			selected != null -> {
				val adapterModule = module {
					single<Adapter> { selected }.bind(adapterClass)
				}
				koin.loadModules(listOf(selected.koinModule, adapterModule))
				return selected
			}
			potential.isEmpty() -> error("No $configKey adapter selected!")
			else -> error("Multiple $configKey adapters selected! ($potential)")
		}
	}
}
